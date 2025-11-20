FROM debian:bookworm-slim AS builder

# Install dependencies
RUN apt-get update && \
    apt-get install -y \
      curl wget unzip ca-certificates git bash \
      && rm -rf /var/lib/apt/lists/*

# --- Install Go (arch-aware) ---
ARG TARGETARCH
ENV GO_VERSION=1.18.10

RUN if [ "$TARGETARCH" = "amd64" ]; then \
           GO_URL="https://go.dev/dl/go${GO_VERSION}.linux-amd64.tar.gz"; \
       elif [ "$TARGETARCH" = "arm64" ]; then \
           GO_URL="https://go.dev/dl/go${GO_VERSION}.linux-arm64.tar.gz"; \
       else \
           echo "Unsupported arch: $TARGETARCH" && exit 1; \
       fi \
    && echo "Downloading Go from $GO_URL" \
    && curl -fsSL "$GO_URL" -o /tmp/go.tar.gz \
    && mkdir -p /opt \
    && tar -C /opt -xzf /tmp/go.tar.gz \
    && rm -f /tmp/go.tar.gz

# --- Install AWS CLI (arch-aware) ---
RUN if [ "$TARGETARCH" = "amd64" ]; then \
        AWS_CLI_URL="https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip"; \
    elif [ "$TARGETARCH" = "arm64" ]; then \
        AWS_CLI_URL="https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip"; \
    else \
        echo "Unsupported arch: $TARGETARCH" && exit 1; \
    fi \
    && echo "Downloading AWS CLI from $AWS_CLI_URL" \
    && curl -fsSL "$AWS_CLI_URL" -o /tmp/awscliv2.zip \
    && unzip -q /tmp/awscliv2.zip -d /tmp \
    && /tmp/aws/install --bin-dir /opt/aws-cli/bin --install-dir /opt/aws-cli \
    && rm -rf /tmp/awscliv2.zip /tmp/aws

ENV PATH="/opt/go/bin:$PATH"
ENV GOPATH="/go"
ENV GOROOT="/opt/go"

# --- Final runtime image ---
FROM debian:bookworm-slim

RUN apt-get update && \
    apt-get install -y ca-certificates bash curl wget git make gcc libc6-dev && \
    rm -rf /var/lib/apt/lists/*

# Copy Go from build stage
COPY --from=builder /opt/go /opt/go

# Create GOPATH directory
RUN mkdir -p /go/bin /go/src /go/pkg

# Copy AWS CLI from build stage
COPY --from=builder /opt/aws-cli /opt/aws-cli

ENV PATH="/opt/go/bin:/go/bin:/opt/aws-cli/bin:$PATH" \
    GOROOT="/opt/go" \
    GOPATH="/go" \
    GO111MODULE=on \
    CGO_ENABLED=1

CMD ["sh", "-c", "go version && echo '---' && go env && echo '---' && aws --version"]
