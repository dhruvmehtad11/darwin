#!/bin/bash

#export env
export ENV='local'
#export token
export VAULT_SERVICE_GITHUB_TOKEN='test_token'

#CONFIGS_MAP['local']['fsx_root']=darwin_workspace
fsx_root=darwin_workspace

#grant modification permission for this directory as this is used to mimic EFS
sudo chmod -R 777 /var/www/

#make directory for mocking efs functionalities
sudo mkdir -p /var/www/$fsx_root

#pull images required for ElasticSearch and MySQL remote server connections
docker-compose -f local/docker-compose.yml  pull

#start the ES and Mysql server containers
docker-compose -f local/docker-compose.yml  up -d

#make file executable
chmod +x ./local/setup_for_sql.sh

echo 'setup for sql starts'

# Setup MySQL server
./local/setup_for_sql.sh

#make file executable
chmod +x ./local/setup_for_elasticsearch.sh

echo 'setup for ES starts'

# Setup ES server
./local/setup_for_elasticsearch.sh

# Start the main server using uvicorn
uvicorn app-layer.src.workspace_app_layer.main:app --host localhost --port 8000 --reload
