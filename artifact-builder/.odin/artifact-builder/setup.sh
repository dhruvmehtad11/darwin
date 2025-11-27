#!/usr/bin/env bash
set -e

# During Docker build, we run as root, so no sudo needed
# Install system dependencies
echo "📦 Installing system dependencies..."
apt-get update -y
apt-get install --assume-yes \
  nfs-common \
  apt-utils \
  ca-certificates \
  curl \
  gnupg \
  lsb-release \
  python3 \
  python3-pip \
  && rm -rf /var/lib/apt/lists/*

# Install Docker CLI (daemon will be started at runtime in start.sh)
echo "🐳 Installing Docker CLI..."
mkdir -m 0755 -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update
apt-get install --assume-yes \
  docker-ce \
  docker-ce-cli \
  containerd.io \
  docker-buildx-plugin \
  docker-compose-plugin \
  && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
echo "📚 Installing Python dependencies..."
cd "$BASE_DIR" || exit 1
# Install in dependency order: model -> core -> app_layer
echo "  Installing model..."
pip3 install -e model/. --force-reinstall 2>&1
echo "  Installing core..."
pip3 install -e core/. --force-reinstall 2>&1
echo "  Installing app_layer..."
pip3 install -e app_layer/. --force-reinstall 2>&1
echo "✅ Python requirements installed"

#
#PIP_CONF_DIR="$HOME/.pip"
#PIP_CONF_PATH="$PIP_CONF_DIR/pip.conf"
#
## Create the ~/.pip directory if it doesn't exist
#mkdir -p "$PIP_CONF_DIR"
#
## Create or overwrite the pip.conf file
#cat > "$PIP_CONF_PATH" <<EOF
#[global]
#index-url = http://pypi-server.darwin.dream11-k8s.local/
#extra-index-url= http://pypi-server.darwin-d11-stag.local/
#trusted-host = pypi-server.darwin.dream11-k8s.local
#               pypi-server.darwin-d11-stag.local
#               pypi.org
#EOF
#
#echo "Created $PIP_CONF_PATH with the specified content"