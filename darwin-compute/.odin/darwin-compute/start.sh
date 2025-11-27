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

export OTEL_SERVICE_NAME=${SERVICE_NAME}
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318

echo "OTEL_SERVICE_NAME: ${OTEL_SERVICE_NAME}"
echo "OTEL_EXPORTER_OTLP_ENDPOINT: ${OTEL_EXPORTER_OTLP_ENDPOINT}"

echo "----- Setup log file path -----"
if [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  mkdir -p log/darwin-compute
  chmod 777 log/darwin-compute/
  export LOG_DIR=/app/log/darwin-compute
else
  mkdir -p /var/log/darwin-compute
  chmod 777 /var/log/darwin-compute/
  export LOG_DIR=/var/log/darwin-compute
fi

echo "Cding into app dir.."
cd "$APP_DIR" || exit

echo "Starting app layer.."
if [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  echo "Starting app layer ..."
  LOG_FILE=$LOG_DIR/compute.log python -m uvicorn app_layer.src.compute_app_layer.main:app --host 0.0.0.0 --port 8000 --workers 2
else
  echo "Cding into app layer"
  cd app_layer/src/compute_app_layer
  CORES=$(nproc)
  echo "Starting app layer ..."
  LOG_FILE=$LOG_DIR/compute.log uvicorn main:app --host 0.0.0.0 --port 8000 --workers $CORES --timeout-keep-alive 75
fi
