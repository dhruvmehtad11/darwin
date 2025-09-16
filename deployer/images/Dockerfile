# Dockerfile
ARG BASE_IMAGE=debian:bookworm-slim
FROM ${BASE_IMAGE} AS base

ARG APP_NAME
ARG APP_BASE_DIR
ARG APP_DIR
WORKDIR /app/

# Copy folder to the image
COPY ${APP_BASE_DIR}/ /app/${APP_BASE_DIR}/

# Copy artifact from build context
COPY deployer/scripts/image-builder.sh /app/

# Build
RUN --mount=type=cache,target=/root/.m2/repository \
  sh image-builder.sh -a ${APP_NAME} -t ${APP_BASE_DIR} -p ${APP_DIR}

RUN rm /app/image-builder.sh
RUN rm -r /app/$APP_BASE_DIR

# Setup
# RUN --mount=type=cache,target=/root/.m2/repository \
#   sh ./.odin/${APP_NAME}/setup.sh

# Entrypoint
ENTRYPOINT ["bash", "-c", "/app/.odin/start.sh"]
