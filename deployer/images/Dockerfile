# Dockerfile
ARG BASE_IMAGE=debian:bookworm-slim
FROM ${BASE_IMAGE} AS base

ARG APP_NAME
ARG APP_BASE_DIR
ARG APP_DIR
WORKDIR /app/

COPY ${APP_BASE_DIR}/${APP_DIR}/target/${APP_NAME}/. /app/

# Setup
# RUN --mount=type=cache,target=/root/.m2/repository \
#   sh ./.odin/${APP_NAME}/setup.sh

# if [[ -f "/app/.odin/setup.sh" ]]; then
#     bash /app/.odin/setup.sh
# fi

# Entrypoint
ENTRYPOINT ["bash", "-c", "/app/.odin/start.sh"]
