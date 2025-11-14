FROM debian:bookworm-slim AS builder

# Install build dependencies
RUN apt-get update && apt-get install -y \
      curl wget build-essential libssl-dev zlib1g-dev \
      libncurses5-dev libncursesw5-dev libreadline-dev \
      libsqlite3-dev libffi-dev libbz2-dev xz-utils \
      && rm -rf /var/lib/apt/lists/*

ARG PYTHON_VERSION=3.9.7

# Download and compile Python
RUN cd /tmp && \
    wget https://www.python.org/ftp/python/${PYTHON_VERSION}/Python-${PYTHON_VERSION}.tgz && \
    tar xzf Python-${PYTHON_VERSION}.tgz && \
    cd Python-${PYTHON_VERSION} && \
    ./configure --enable-optimizations --prefix=/usr/local && \
    make -j$(nproc) && make altinstall && \
    cd / && rm -rf /tmp/Python-${PYTHON_VERSION}*

# Create python3 symlink and install pip
RUN ln -s /usr/local/bin/python3.9 /usr/local/bin/python3 && \
    ln -s /usr/local/bin/python3.9 /usr/local/bin/python && \
    curl -sSL https://bootstrap.pypa.io/get-pip.py | python3.9

# --- Final runtime image ---
FROM debian:bookworm-slim

# Install runtime dependencies only
RUN apt-get update && apt-get install -y \
      ca-certificates curl git \
      libssl3 libffi8 libsqlite3-0 libbz2-1.0 \
      default-mysql-client \
      && rm -rf /var/lib/apt/lists/*

# Copy Python installation from builder
COPY --from=builder /usr/local /usr/local

# Upgrade pip and install common packages
RUN python3 -m pip install --upgrade pip setuptools wheel

# Set environment variables
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PATH="/usr/local/bin:$PATH"

CMD ["sh", "-c", "python3 --version && pip --version"]
