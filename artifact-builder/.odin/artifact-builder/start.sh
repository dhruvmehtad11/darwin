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
  mkdir -p log/artifact-builder
  chmod 777 log/artifact-builder/
  export LOG_DIR=/app/log/artifact-builder
else
  mkdir -p /var/log/artifact-builder
  chmod 777 /var/log/artifact-builder/
  export LOG_DIR=/var/log/artifact-builder
fi

echo "Cding into app dir.."
cd "$APP_DIR" || exit

echo "Cding into app layer"
cd app_layer/src/serve_app_layer


echo "----- Setup log file path -----"

mkdir -p logs
chmod 777 logs

echo "----- Start Docker daemon -----"
service docker start || true
# Wait a moment for Docker daemon to be ready
sleep 2

# The AWS and GCP credentials are now expected to be in the environment,
# passed from the Kubernetes deployment (values.yaml).
# The envconsul logic has been removed for simplification in local/OSS setup.

echo "Verifying AWS credentials from environment..."
echo "AWS_ACCESS_KEY_ID is set: ${AWS_ACCESS_KEY_ID:+'true'}"
# Note: Don't print the actual keys in logs.

echo "Starting app layer"

LOG_FILE=$LOG_DIR/artifact-builder.log python3 main.py 2>&1 | tee "$LOG_FILE"