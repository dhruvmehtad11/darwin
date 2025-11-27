sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose version

docker run --name compute-mysql -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=$VAULT_SERVICE_MYSQL_PASSWORD -d mysql:latest
docker-compose -f app_layer/tests/elasticsearch.yml up -d
docker-compose -f app_layer/tests/kafka.yml up -d