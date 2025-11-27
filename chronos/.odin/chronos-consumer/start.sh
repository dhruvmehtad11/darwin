#!/usr/bin/env bash
echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "SERVICE_NAME: ${SERVICE_NAME}"
echo "PLATFORM: ${PLATFORM}"
echo "DEPLOYMENT_TYPE: ${DEPLOYMENT_TYPE}"
echo "NAMESPACE: ${NAMESPACE}"

export OTEL_SERVICE_NAME=${SERVICE_NAME}
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318

echo "OTEL_SERVICE_NAME: ${OTEL_SERVICE_NAME}"
echo "OTEL_EXPORTER_OTLP_ENDPOINT: ${OTEL_EXPORTER_OTLP_ENDPOINT}"

echo "Cding into app dir.."
cd "$APP_DIR"

echo "QUEUE_STRATEGY: ${QUEUE}"

if [[ "$QUEUE" == "KAFKA" ]]; then
  echo "🔧 Running Kafka setup for local environment..."
  bash scripts/setup_kafka_local.sh
else
  echo "🔧 Running SQS setup for local environment..."
  bash scripts/setup_sqs_local.sh
fi
mkdir -p /tmp/logs
LOG_DIR=/tmp/logs
touch /tmp/logs/chronos_consumer.log
LOG_FILE=$LOG_DIR/chronos_consumer.log uvicorn src.start_consumer:app --host 0.0.0.0 --port 8000 --workers 4 2>&1
