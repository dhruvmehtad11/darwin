#!/usr/bin/env bash
set -e
echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "VPC_SUFFIX: ${VPC_SUFFIX}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "SERVICE_NAME: ${SERVICE_NAME}"
echo "PLATFORM: ${PLATFORM}"
echo "DEPLOYMENT_TYPE: ${DEPLOYMENT_TYPE}"
echo "NAMESPACE: ${NAMESPACE}"

echo "----- Setup log file path -----"
mkdir -p /var/log/darwin-workflow
chmod 777 /var/log/darwin-workflow/

echo "----- Attach FSx -----"
# Skip FSx mounting for local development
if [[ "$ENV" == "darwin-local" ]]; then
    echo "Skipping FSx mount for local environment"
    CONSUL_ADDR="localhost"
    VAULT_ADDR="http://localhost:8200"
else
    cd /var/www
    sudo mkdir -p fsx
    chmod 777 /var/www/fsx/

    if [[ "$ENV" == "prod" || "$ENV" == "uat" ]]; then
        CONSUL_ADDR="config-store-${ENV}.dream11.com"
        VAULT_ADDR="http://secret-store-${ENV}.dream11.com"
        sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport 10.204.2.172:/ fsx
    else
        CONSUL_ADDR="config-store-${ENV}.d11dev.com"
        VAULT_ADDR="http://secret-store-${ENV}.d11dev.com"
        sudo mount -t nfs4 -o nfsvers=4.1,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport 10.28.182.106:/ fsx
    fi
fi

echo "Cding into app dir.."
cd "${APP_DIR}" || exit

CONFIG_OPTS="-upcase -sanitize -flatten -consul-addr ${CONSUL_ADDR} -vault-addr ${VAULT_ADDR} -vault-renew-token=false -prefix d11/${NAMESPACE} -secret secrets/data/d11/${NAMESPACE}/${SERVICE_NAME}/${ENV}/default"
echo ${CONFIG_OPTS}
echo "----- Set AWS variables -----"
whoami

# Handle local development environment
if [[ "$ENV" == "darwin-local" ]]; then
    echo "Using local development AWS credentials"
    export AWS_ACCESS_KEY_ID="minioadmin"
    export AWS_SECRET_ACCESS_KEY="minioadmin"
    export AWS_DEFAULT_REGION="us-east-1"
else
    # Use envconsul for non-local environments
    ACCESS_KEY=$(envconsul ${CONFIG_OPTS} env | grep -E '^VAULT_SERVICE_ACCESS_KEY_ID=' | cut -d "=" -f2)
    SECRET_KEY=$(envconsul ${CONFIG_OPTS} env | grep -E '^VAULT_SERVICE_SECRET_ACCESS_KEY=' | cut -d "=" -f2)
    DEF_REGION=$(envconsul ${CONFIG_OPTS} env | grep -E '^VAULT_SERVICE_DEFAULT_REGION=' | cut -d "=" -f2)
    export AWS_ACCESS_KEY_ID=$ACCESS_KEY
    export AWS_SECRET_ACCESS_KEY=$SECRET_KEY
    export AWS_DEFAULT_REGION=$DEF_REGION
fi

echo "AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID}"
echo "AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY}"
echo "AWS_DEFAULT_REGION: ${AWS_DEFAULT_REGION}"
echo "----- AWS caller identity -----"

if [[ "$ENV" != "darwin-local" ]]; then
  aws sts get-caller-identity
fi

cd "$APP_DIR" || exit
export LOG_DIR=/var/log/darwin-workflow

echo "Cding into app layer"
cd app_layer/src/workflow_app_layer

echo "Starting app layer"
CORES=$(nproc)
# Limit workers for local development to avoid database initialization conflicts
if [[ "$ENV" == "darwin-local" ]]; then
  WORKERS=3
elif [[ "$ENV" == "prod" || "$ENV" == "uat" ]]; then
  WORKERS=$CORES
else
  WORKERS=$((CORES > 4 ? 4 : CORES))
fi

if [[ "$ENV" == "prod" || "$ENV" == "uat" ]]; then
  LOG_FILE=$LOG_DIR/workflow.log envconsul ${CONFIG_OPTS} uvicorn main:app --host 0.0.0.0 --port 8000 --workers $WORKERS --timeout-keep-alive 75
elif [[ "$ENV" == "darwin-local" ]]; then
  LOG_FILE=$LOG_DIR/workflow.log uvicorn main:app --host 0.0.0.0 --port 8000 --workers $WORKERS --timeout-keep-alive 75
else
  VPC_SUFFIX="$VPC_SUFFIX" TEAM_SUFFIX="$TEAM_SUFFIX" LOG_FILE=$LOG_DIR/workflow.log envconsul ${CONFIG_OPTS} uvicorn main:app --host 0.0.0.0 --port 8000 --workers $WORKERS --timeout-keep-alive 75
fi
echo "app layer started"
