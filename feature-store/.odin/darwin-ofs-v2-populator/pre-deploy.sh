#!/usr/bin/env bash

set -e

echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "VPC_SUFFIX: ${VPC_SUFFIX}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "SERVICE_NAME: ${SERVICE_NAME}"

if [[ "$ENV" != darwin-local ]] ; then
  mvn com.dream11:config-maven-plugin:1.3.2:init
  source ./.config
fi

# For production like environments only check if migrations are already run. We intend to run migrations manually or via mydbops
if [[ "$ENV" == prod* ]] || [[ "$ENV" == uat* ]]; then
  echo 'Not using migrations in production'
else
  java \
      -Dapp.environment=${ENV} \
      --add-opens=java.base/java.lang=ALL-UNNAMED \
      -cp ${SERVICE_NAME}-fat.jar \
      com.dream11.populator.util.HelixClusterUtils create-cluster
fi
