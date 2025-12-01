#!/usr/bin/env bash

set -e
echo "APP_DIR: ${APP_DIR}"
echo "ENV: ${ENV}"
echo "VPC_SUFFIX: ${VPC_SUFFIX}"
echo "TEAM_SUFFIX: ${TEAM_SUFFIX}"
echo "SERVICE_NAME: ${SERVICE_NAME}"
echo "DEPLOYMENT_TYPE: ${DEPLOYMENT_TYPE}"

JAVA_OPTS="-XX:+HeapDumpOnOutOfMemoryError"
DATADOG_OPTS="-javaagent:/opt/datadog/dd-java-agent.jar -Ddd.logs.injection=true"
JMX_OPTS="-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
APP_OPTS="-Dapp.environment=${ENV} -DVPC_SUFFIX=${VPC_SUFFIX} -DTEAM_SUFFIX=${TEAM_SUFFIX} -Dvertx.disableDnsResolver=true -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"

if [[ "${DEPLOYMENT_TYPE}" == "container" ]]; then
  LOG_OPTS="-Dlog.directory.path=/app/logs -Dlog.file.name=${SERVICE_NAME}.log -Dlogback.configurationFile=${APP_DIR}/resources/logback/logback-docker.xml"
  MEM_OPTS="-Xms512m -Xmx512m -Xmn256m"
else
  LOG_OPTS="-Dlog.directory.path=/opt/logs -Dlog.file.name=${SERVICE_NAME}.log -Dlogback.configurationFile=${APP_DIR}/resources/logback/logback.xml"
  totalMem=${TOTAL_MEMORY:-$(free -m | head -2 | tail -1 | awk '{print $2}')}
  heapSize=$((totalMem * 70 / 100))
  halfHeapSize=$((heapSize / 2))
  MEM_OPTS="-Xms${heapSize}m -Xmx${heapSize}m -Xmn${halfHeapSize}m"
  sed -i 's/apm_config:/apm_config:\n  probabilistic_sampler:\n    enabled: true\n    sampling_percentage: 1/1' /etc/datadog-agent/datadog.yaml
  sudo systemctl restart datadog-agent
fi

PATH_TO_APP_JAR="${APP_DIR}/${SERVICE_NAME}-fat.jar"

cd "${APP_DIR}" || exit

if [[ "${DEPLOYMENT_TYPE}" == "container" ]]; then
  TEAM_SUFFIX=${TEAM_SUFFIX} VPC_SUFFIX=${VPC_SUFFIX} java -jar ${JAVA_OPTS} ${MEM_OPTS} \
  ${JMX_OPTS} ${APP_OPTS} ${LOG_OPTS} ${PATH_TO_APP_JAR}
else
  TEAM_SUFFIX=${TEAM_SUFFIX} VPC_SUFFIX=${VPC_SUFFIX} nohup java -jar ${JAVA_OPTS} ${MEM_OPTS} ${DATADOG_OPTS} \
  ${JMX_OPTS} ${APP_OPTS} ${LOG_OPTS} ${PATH_TO_APP_JAR} </dev/null >/dev/null 2>&1 &
  pid=$!
  [ -n "$PID_PATH" ] && echo $pid >"${PID_PATH}"
fi

###### discovery
DISCOVERY_HOST="darwin-ofs-v2-populator$TEAM_SUFFIX.dream11$VPC_SUFFIX.local"

chmod +x resources/scripts/register-ip.sh
./resources/scripts/register-ip.sh "us-east-1" $ENV $ARTIFACT_NAME $ARTIFACT_VERSION $DISCOVERY_HOST
