#!/bin/sh
set -e

echo '' > config.env

ENV=local
ENV_CREATION=false

# Check if ENV environment variable equals "local"
if [ "$ENV" = "local" ]; then
    echo "ENV is set to 'local'"
    # Start the kind cluster using the existing script
    read -p "Do you want to setup local k8s cluster? (y/n) " -n 1 -r
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
        echo "\nStarting kind cluster..."

        envsubst < ./kind/kind-config.yaml > ./kind/kind-config-tmp.yaml
        export CLUSTER_NAME=kind
        export KIND_CONFIG=./kind/kind-config-tmp.yaml
        export KUBECONFIG=./kind/config/kindkubeconfig.yaml
        
        sh ./kind/start-cluster.sh
        ENV_CREATION=true
        echo "DOCKER_REGISTRY=localhost:5000" >> config.env

        rm ./kind/kind-config-tmp.yaml
    else
        echo "\nSkipping kind cluster setup"
        echo "DOCKER_REGISTRY=docker.io" >> config.env
    fi
else
    echo "ENV is not set to 'local' (current value: '$ENV'), skipping local k8s cluster setup"
fi

# check if kube config file exists and is reachable
if [ ! -f "$KUBECONFIG" ]; then
    echo "KUBECONFIG file does not exist"
    exit 1
else
    echo "KUBECONFIG=$KUBECONFIG" >> config.env
    source config.env
fi


if kubectl version >/dev/null 2>&1; then
  echo "✅ Cluster is up"
else
  echo "❌ Cluster is not reachable"
fi

# Ask if user wants a clean build
read -p "Do you want a clean build? (y/n) " -n 1 -r
if [[ ! $REPLY =~ ^[Yy]$ ]]
then
    echo "\nSkipping build. Exiting."
    exit 0
fi
echo

# Install yq if not available
if ! command -v yq >/dev/null 2>&1; then
  echo "Installing yq..."
  
  # Detect OS
  OS=$(uname -s | tr '[:upper:]' '[:lower:]')
  case "$OS" in
    darwin) OS="darwin" ;;
    linux) OS="linux" ;;
    *) echo "Unsupported OS: $OS. Please install yq manually."; exit 1 ;;
  esac
  
  # Detect architecture
  ARCH=$(uname -m)
  case "$ARCH" in
    x86_64) ARCH="amd64" ;;
    amd64) ARCH="amd64" ;;
    arm64) ARCH="arm64" ;;
    aarch64) ARCH="arm64" ;;
    *) echo "Unsupported architecture: $ARCH. Please install yq manually."; exit 1 ;;
  esac
  
  # Download yq binary
  YQ_URL="https://github.com/mikefarah/yq/releases/latest/download/yq_${OS}_${ARCH}"
  echo "Downloading yq from: $YQ_URL"
  
  # Create directory if it doesn't exist
  mkdir -p /usr/local/bin
  
  # Download and install
  if curl -fsSL "$YQ_URL" -o /usr/local/bin/yq; then
    chmod +x /usr/local/bin/yq
    echo "✅ yq installed successfully"
  else
    echo "❌ Failed to download yq. Please install manually."
    exit 1
  fi
else
  echo "✅ yq is already available"
fi

pushd deployer/images/java-11
sh build.sh
popd

pushd deployer/images/python-3.9.7
sh build.sh
popd

# Path to your YAML
YAML_FILE="services.yaml"

# Loop through YAML using array approach
app_count=$(yq eval '.applications | length' "$YAML_FILE")
i=0
while [ $i -lt $app_count ]; do
  application=$(yq eval ".applications[$i].application" "$YAML_FILE")
  base_path=$(yq eval ".applications[$i].base-path" "$YAML_FILE")
  path=$(yq eval ".applications[$i].path" "$YAML_FILE")
  base_image=$(yq eval ".applications[$i].base-image" "$YAML_FILE")

  echo ">>> Building image for $application..."
  sh deployer/scripts/image-builder.sh -a "$application" -t "$base_path" -p "$path" -e "$base_image"
  
  echo ">>> Completed processing $application"
  i=$((i + 1))
done
