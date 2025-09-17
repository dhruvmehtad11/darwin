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
        rm ./kind/kind-config-tmp.yaml
    else
        echo "\nSkipping kind cluster setup"
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

# Loop through YAML using yq  
yq eval '.applications[] | @json' "$YAML_FILE" | while read -r app; do
  application=$(echo "$app" | yq eval '.application' - -p json)
  base_path=$(echo "$app" | yq eval '.["base-path"]' - -p json)
  path=$(echo "$app" | yq eval '.path' - -p json)
  image=$(echo "$app" | yq eval '.image' - -p json)

  echo ">>> Building image for $application..."
  sh deployer/scripts/image-builder.sh -a "$application" -t "$base_path" -p "$path" -e "$image"

  # if [ "$ENV_CREATION" = "true" ]; then
  #   echo ">>> Loading image $application:latest into kind cluster $CLUSTER_NAME..."
  #   kind load docker-image "$application:latest" --name "$CLUSTER_NAME"
  # fi
  
  echo ">>> Completed processing $application"
done
