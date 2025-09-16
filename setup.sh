#!/bin/sh
set -e

echo '' > config.env

# Check if ENV environment variable equals "local"
if [ "$ENV" = "local" ]; then
    echo "ENV is set to 'local'"
    # Start the kind cluster using the existing script
    read -p "Do you want to setup local k8s cluster? (y/n)" -n 1 -r
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
        echo "\nStarting kind cluster..."

        export CLUSTER_NAME=kind
        export KIND_CONFIG=./kind/kind-config.yaml
        export KUBECONFIG=./kind/config/kindkubeconfig.yaml
        sh ./kind/start-cluster.sh
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

pushd deployer/images/java-11
sh build.sh
popd

pushd deployer/images/python-3.9.7
sh build.sh
popd

sudo docker build \
  --build-arg BASE_IMAGE=darwin/java:11-maven-bookworm-slim \
  --build-arg APP_NAME=darwin-ofs-v2 \
  --build-arg APP_BASE_DIR=feature-store \
  --build-arg APP_DIR=app \
  -t darwin-ofs-v2:latest \
  -f deployer/images/Dockerfile .