#!/bin/sh

# Source the config.env file
set -o allexport
source config.env
set +o allexport

# Variables
APP_DIR="/app"
ENV="darwin-local"
VPC_SUFFIX="-darwin-local"
TEAM_SUFFIX="-darwin-local"
DEPLOYMENT_TYPE="container"


SERVICE_NAME="darwin-ofs-v2"
# kubectl run ${SERVICE_NAME}-pre-deploy --rm -it \
#   --image=darwin-ofs-v2:latest \
#   --image-pull-policy=IfNotPresent \
#   --restart=Never \
#   --env="APP_DIR=${APP_DIR}" \
#   --env="ENV=${ENV}" \
#   --env="VPC_SUFFIX=${VPC_SUFFIX}" \
#   --env="TEAM_SUFFIX=${TEAM_SUFFIX}" \
#   --env="SERVICE_NAME=${SERVICE_NAME}" \
#   --env="DEPLOYMENT_TYPE=${DEPLOYMENT_TYPE}" \
#   --command -- bash -c ".odin/pre-deploy.sh"

# kubectl run test-pod --rm -it \
#   --image=darwin-ofs-v2:latest \
#   --restart=Never \
#   --image-pull-policy=IfNotPresent \
#   --env="APP_DIR=/app" \
#   --env="ENV=local" \
#   --env="VPC_SUFFIX=" \
#   --env="TEAM_SUFFIX=" \
#   --env="SERVICE_NAME=darwin-ofs-v2" \
#   --env="DEPLOYMENT_TYPE=container" \
#   --command -- bash -c ".odin/pre-deploy.sh" 

# kubectl run test-pod --rm -it \
#   --namespace=darwin \
#   --image=darwin-ofs-v2-admin:latest \
#   --restart=Never \
#   --image-pull-policy=IfNotPresent \
#   --overrides='
# {
#   "spec": {
#     "containers": [{
#       "name": "test-pod",
#       "image": "darwin-ofs-v2-admin:latest",
#       "imagePullPolicy": "IfNotPresent",
#       "command": ["bash", "-c", ".odin/pre-deploy.sh"],
#       "env": [
#         {"name":"APP_DIR","value":"/app"},
#         {"name":"ENV","value":"darwin-local"},
#         {"name":"VPC_SUFFIX","value":"-darwin-local"},
#         {"name":"TEAM_SUFFIX","value":"-darwin-local"},
#         {"name":"SERVICE_NAME","value":"darwin-ofs-v2"},
#         {"name":"DEPLOYMENT_TYPE","value":"container"},
#         {"name":"DARWIN_MYSQL_HOST","value":"darwin-mysql"},
#         {"name":"DARWIN_CASSANDRA_HOST","value":"darwin-cassandra"},
#         {"name":"DARWIN_MYSQL_USERNAME","value":"root"},
#         {"name":"DARWIN_MYSQL_PASSWORD","value":"password"},
#         {"name":"VAULT_SERVICE_MYSQL_PASSWORD","value":"username"},
#         {"name":"VAULT_SERVICE_MYSQL_USERNAME","value":"password"}
#       ],
#       "volumeMounts": [{
#         "mountPath": "/root/.m2/",
#         "name": "host-data"
#       }]
#     }],
#     "volumes": [{
#       "name": "host-data",
#       "hostPath": {
#         "path": "/mnt/shared-data/.m2/", 
#         "type": "Directory"
#       }
#     }]
#   }
# }'

