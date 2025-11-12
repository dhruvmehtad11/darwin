# Dockerfile
ARG BASE_IMAGE=debian:bookworm-slim
FROM ${BASE_IMAGE} AS base

ARG APP_NAME
ARG APP_BASE_DIR
ARG APP_DIR
ARG EXTRA_ENV_VARS str 
WORKDIR /app/


# Append BASE_DIR, SERVICE_NAME, DEPLOYMENT_TYPE, APP_DIR to EXTRA_ENV_VARS
ARG EXTRA_ENV_VARS_FINAL
ENV EXTRA_ENV_VARS_FINAL="${EXTRA_ENV_VARS}|BASE_DIR=${APP_BASE_DIR}|SERVICE_NAME=${APP_NAME}|DEPLOYMENT_TYPE=container|APP_DIR=${APP_DIR}"
RUN echo "EXTRA_ENV_VARS_FINAL: ${EXTRA_ENV_VARS_FINAL}"

COPY ${APP_BASE_DIR}/${APP_DIR}/target/${APP_NAME}/. /app/


RUN echo "EXTRA_ENV_VARS_FINAL: ${EXTRA_ENV_VARS_FINAL}"

ENV DEPLOYMENT_TYPE="container"
# Run setup.sh if it exists
RUN if [ -f "/app/.odin/setup.sh" ]; then \
      env $(echo ${EXTRA_ENV_VARS_FINAL} | tr '|' ' ') bash -x /app/.odin/setup.sh; \
    fi

# Entrypoint
ENTRYPOINT ["bash", "-c", "/app/.odin/start.sh"]
