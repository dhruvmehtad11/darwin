FROM rayproject/ray-ml:2.47.1.61d3f2-py310-gpu

RUN sudo apt-get update -y && sudo apt-get install -y openjdk-11-jdk zip unzip curl telnet dnsutils iputils-ping nodejs vim nano htop sed watch build-essential apache2-utils lsof maven && sudo apt-get clean

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash \
        && export NVM_DIR="$HOME/.nvm" \
        && [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" \
        && [ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion" \
        && nvm install 20.12.0 \
        && nvm use 20.12.0 \
        && node -v \
        && ARCH=$(uname -m) && \
        if [ "$ARCH" = "aarch64" ] || [ "$ARCH" = "arm64" ]; then \
            AWS_ARCH="aarch64"; \
        else \
            AWS_ARCH="x86_64"; \
        fi && \
        curl "https://awscli.amazonaws.com/awscli-exe-linux-${AWS_ARCH}.zip" -o "awscliv2.zip" \
        && unzip awscliv2.zip \
        && sudo ./aws/install \
        && sudo rm -rf awscliv2.zip \
        && sudo rm -rf ./aws \
    && sudo mkdir /raydp \
        && sudo chown -R ray /raydp \
    && HOROVOD_WITH_TENSORFLOW=1 \
    && HOROVOD_WITHPYTORCH=1 \
    && HOROVOD_WITH_GLOO=1

RUN pip install backoff==1.10.0 \
    jupyterlab==4.3.0 \
    lxml \
    papermill

# Disable Ray usage stats
RUN ray disable-usage-stats
