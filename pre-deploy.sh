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

kubectl run test-pod --rm -it \
  --image=darwin-ofs-v2:latest \
  --restart=Never \
  --image-pull-policy=IfNotPresent \
  --env="APP_DIR=/app" \
  --env="ENV=local" \
  --env="VPC_SUFFIX=" \
  --env="TEAM_SUFFIX=" \
  --env="SERVICE_NAME=darwin-ofs-v2" \
  --env="DEPLOYMENT_TYPE=container" \
  --overrides='
{
  "spec": {
    "containers": [{
      "name": "test-pod",
      "image": "darwin-ofs-v2:latest",
      "command": ["bash", "-c", ".odin/pre-deploy.sh"],
      "env": [
        {"name":"APP_DIR","value":"/app"},
        {"name":"ENV","value":"local"},
        {"name":"VPC_SUFFIX","value":""},
        {"name":"TEAM_SUFFIX","value":""},
        {"name":"SERVICE_NAME","value":"darwin-ofs-v2"},
        {"name":"DEPLOYMENT_TYPE","value":"container"}
      ],
      "volumeMounts": [{
        "mountPath": "/root/.m2",
        "name": "host-data"
      }]
    }],
    "volumes": [{
      "name": "host-data",
      "hostPath": {
        "path": "/mnt/shared-data/.m2", 
        "type": "Directory"
      }
    }]
  }
}'

