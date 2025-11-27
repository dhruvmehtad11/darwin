#!/usr/bin/env bash

echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "VPC_SUFFIX: ${VPC_SUFFIX}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "SERVICE_NAME: ${SERVICE_NAME}"
echo "PLATFORM: ${PLATFORM}"
echo "TOTAL_MEMORY: ${TOTAL_MEMORY}"
echo "DEPLOYMENT_TYPE: ${DEPLOYMENT_TYPE}"

export LOG_TO_FILE=1
export OTEL_EXPORTER_OTLP_ENDPOINT=localhost:4317

if [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  DIR_PREFIX="/app/"
  export PATH=$PATH:/home/app/bin
else
  DIR_PREFIX="/var/www/"
fi

cd "${APP_DIR}" || exit

echo "----- Setup log file path -----"
if [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  mkdir -p log/darwin-cluster-manager
  chmod 777 log/darwin-cluster-manager/
  export LOG_DIR=/app/log/darwin-cluster-manager
else
  mkdir -p /var/log/darwin-cluster-manager
  chmod 777 /var/log/darwin-cluster-manager/
  export LOG_DIR=/var/log/darwin-cluster-manager
fi
export LOG_FILE="${LOG_DIR}/cluster_manager.log"

echo "----- Make dir for artifacts -----"
mkdir -p tmp
mkdir -p tmp/artifacts
mkdir -p tmp/values

echo "----- Set AWS variables -----"
whoami
echo "AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID}"
echo "AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY}"
echo "AWS_DEFAULT_REGION: ${AWS_DEFAULT_REGION}"

echo "----- Run cluster manager -----"
ENV=$ENV ./cluster_manager
