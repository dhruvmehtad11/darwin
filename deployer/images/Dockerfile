# Dockerfile
ARG BASE_IMAGE=debian:bookworm-slim
FROM ${BASE_IMAGE} AS base

ARG APP_NAME
ARG APP_BASE_DIR
ARG APP_DIR
WORKDIR /app/

COPY ${APP_BASE_DIR}/${APP_DIR}/target/${APP_NAME}/. /app/

RUN chmod +x /app/.odin/*.sh 2>/dev/null || true

# Run setup.sh if it exists
RUN if [ -f "/app/.odin/setup.sh" ]; then \
      bash /app/.odin/setup.sh; \
    fi

# Entrypoint
ENTRYPOINT ["bash", "-c", "/app/.odin/start.sh"]
