# Dockerfile
ARG BASE_IMAGE=debian:bookworm-slim
FROM ${BASE_IMAGE} AS base

ARG APP_NAME
ARG APP_BASE_DIR
ARG APP_DIR
ARG DEPLOYMENT_TYPE
ARG EXTRA_ENV_VARS str 
WORKDIR /app/


# Append BASE_DIR, SERVICE_NAME, DEPLOYMENT_TYPE, APP_DIR to EXTRA_ENV_VARS
ARG EXTRA_ENV_VARS_FINAL
ENV EXTRA_ENV_VARS_FINAL="${EXTRA_ENV_VARS}|BASE_DIR=${APP_BASE_DIR}|SERVICE_NAME=${APP_NAME}|DEPLOYMENT_TYPE=${DEPLOYMENT_TYPE}|APP_DIR=${APP_DIR}"
RUN echo "EXTRA_ENV_VARS_FINAL: ${EXTRA_ENV_VARS_FINAL}"

COPY ${APP_BASE_DIR}/${APP_DIR}/target/${APP_NAME}/. /app/

RUN chmod +x /app/.odin/*.sh 2>/dev/null || true

RUN echo "EXTRA_ENV_VARS_FINAL: ${EXTRA_ENV_VARS_FINAL}"

# Run setup.sh if it exists
# Set environment variables needed by setup.sh
ENV BASE_DIR=/app
ENV DEPLOYMENT_TYPE=container
RUN if [ -f "/app/.odin/setup.sh" ]; then \
      echo "${EXTRA_ENV_VARS_FINAL}" | tr '|' '\n' > /tmp/env_vars.txt && \
      set -a && . /tmp/env_vars.txt && set +a && \
      bash -x /app/.odin/setup.sh; \
    fi

# Create static directory for workflow (if it doesn't exist)
RUN mkdir -p /app/app_layer/src/workflow_app_layer/static 2>/dev/null || true
RUN mkdir -p /app/static 2>/dev/null || true

# Install workflow application packages
RUN if [ -d "/app/app_layer" ] && [ -d "/app/core" ]; then \
      echo "📦 Installing darwin-workflow core dependencies..." && \
      grep -v "mlp-commons-feat-kafka" /app/core/requirements.txt | pip3 install -r /dev/stdin && \
      echo "📦 Installing darwin-workflow app_layer dependencies..." && \
      grep -v "^-e" /app/app_layer/requirements.txt | pip3 install -r /dev/stdin && \
      echo "📦 Installing darwin-workflow packages (using mock dependencies)..." && \
      pip3 install -e /app/core/. --no-deps && \
      pip3 install -e /app/app_layer/. --no-deps && \
      if [ -d "/app/model" ]; then pip3 install -e /app/model/. --no-deps; fi && \
      echo "✅ Darwin-workflow packages installed"; \
    fi

# Entrypoint
ENTRYPOINT ["bash", "-c", "/app/.odin/start.sh"]
