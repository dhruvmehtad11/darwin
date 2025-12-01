#!/usr/bin/env bash
set -e

# Set defaults for local execution
APP_DIR=${APP_DIR:-./target/catalog}
ENV=${ENV:-local}
VPC_SUFFIX=${VPC_SUFFIX:-}
TEAM_SUFFIX=${TEAM_SUFFIX:-}
SERVICE_NAME=${SERVICE_NAME:-catalog}
DEPLOYMENT_TYPE=${DEPLOYMENT_TYPE:-local}

echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "VPC_SUFFIX: ${VPC_SUFFIX}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "SERVICE_NAME: ${SERVICE_NAME}"
echo "DEPLOYMENT_TYPE: ${DEPLOYMENT_TYPE}"

JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=50"
OTEL_INSTRUMENTATION_COMMON_DEFAULT_ENABLED=${OTEL_INSTRUMENTATION_COMMON_DEFAULT_ENABLED:-true}
OTEL_INSTRUMENTATION_VERTX_SQL_CLIENT_ENABLED=${OTEL_INSTRUMENTATION_VERTX_SQL_CLIENT_ENABLED:-true}
OTEL_EXPORTER_OTLP_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT:-http://localhost:4318}

# Make OpenTelemetry agent optional (only include if it exists)
OTEL_AGENT_PATH="/opt/otelcol-contrib/opentelemetry-javaagent.jar"
if [[ -f "$OTEL_AGENT_PATH" ]]; then
  OTEL_OPTS="-javaagent:${OTEL_AGENT_PATH} -Dotel.exporter.otlp.endpoint=${OTEL_EXPORTER_OTLP_ENDPOINT} -Dotel.instrumentation.runtime-metrics.enabled=true -Dotel.metrics.exporter=otlp"
else
  echo "Warning: OpenTelemetry agent not found at ${OTEL_AGENT_PATH}, skipping OTEL instrumentation"
  OTEL_OPTS=""
fi

JMX_OPTS="-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dspring.profiles.active=${ENV}"
APP_OPTS="-Dapp.environment=${ENV} -DVPC_SUFFIX=${VPC_SUFFIX} -DTEAM_SUFFIX=${TEAM_SUFFIX}"

# For container deployments, use /app/app.jar (standard Docker location)
# For local deployments, use the SERVICE_NAME-based path
if [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  PATH_TO_APP_JAR="/app/app.jar"
else
  PATH_TO_APP_JAR="${APP_DIR}/${SERVICE_NAME}-fat.jar"
fi

if [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  LOG_OPTS="-Dlogging.file.path=/app/logs -Dlogging.file.name=${SERVICE_NAME}.log"
  MEM_OPTS="-Xms512m -Xmx512m -Xmn256m"
  echo "TEAM_SUFFIX=${TEAM_SUFFIX} VPC_SUFFIX=${VPC_SUFFIX} java -jar ${JAVA_OPTS} ${MEM_OPTS} ${OTEL_OPTS} ${JMX_OPTS} ${APP_OPTS} ${LOG_OPTS} ${PATH_TO_APP_JAR}"
else
  # For local execution, use local logs directory
  LOG_OPTS="-Dlogging.file.path=./logs -Dlogging.file.name=${SERVICE_NAME}.log"
  mkdir -p ./logs
  
  # Calculate memory based on OS (macOS vs Linux)
  if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS - use vm_stat or default to 1GB
    totalMemMB=1024
    if command -v sysctl &> /dev/null; then
      totalMemBytes=$(sysctl -n hw.memsize)
      totalMemMB=$((totalMemBytes / 1024 / 1024))
    fi
  else
    # Linux - use free command
    if command -v free &> /dev/null; then
      totalMemMB=$(free -m | head -2 | tail -1 | awk '{print $2}')
    else
      totalMemMB=1024
    fi
  fi
  
  heapSize=$((totalMemMB * 70 / 100))
  halfHeapSize=$((heapSize / 2))
  # Ensure minimum heap size
  if [[ $heapSize -lt 512 ]]; then
    heapSize=512
    halfHeapSize=256
  fi
  MEM_OPTS="-Xms${heapSize}m -Xmx${heapSize}m -Xmn${halfHeapSize}m"
fi

if [[ ! -f "$PATH_TO_APP_JAR" ]]; then
  echo "Error: JAR file not found at ${PATH_TO_APP_JAR}"
  echo "Please run build.sh first to create the artifact"
  exit 1
fi

cd "${APP_DIR}" || exit
echo "Starting $SERVICE_NAME ..."

if [[ "$DEPLOYMENT_TYPE" == "container" ]]; then
  TEAM_SUFFIX=${TEAM_SUFFIX} VPC_SUFFIX=${VPC_SUFFIX} nohup java -jar ${JAVA_OPTS} ${MEM_OPTS} ${OTEL_OPTS} \
    ${JMX_OPTS} ${APP_OPTS} ${LOG_OPTS} ${PATH_TO_APP_JAR}
else
  # For local execution, run in foreground by default, or background if BACKGROUND=true
  if [[ "${BACKGROUND:-false}" == "true" ]]; then
    TEAM_SUFFIX=${TEAM_SUFFIX} VPC_SUFFIX=${VPC_SUFFIX} nohup java -jar ${JAVA_OPTS} ${MEM_OPTS} ${OTEL_OPTS} \
      ${JMX_OPTS} ${APP_OPTS} ${LOG_OPTS} ${PATH_TO_APP_JAR} </dev/null >./logs/${SERVICE_NAME}.out 2>&1 &
    pid=$!
    echo "Started $SERVICE_NAME with PID: $pid"
    [ -n "$PID_PATH" ] && echo $pid >"${PID_PATH}"
  else
    # Run in foreground for local development
    TEAM_SUFFIX=${TEAM_SUFFIX} VPC_SUFFIX=${VPC_SUFFIX} java -jar ${JAVA_OPTS} ${MEM_OPTS} ${OTEL_OPTS} \
      ${JMX_OPTS} ${APP_OPTS} ${LOG_OPTS} ${PATH_TO_APP_JAR}
  fi
fi