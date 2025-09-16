#!/bin/sh

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
        ./kind/start-cluster.sh
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
fi

if kubectl version >/dev/null 2>&1; then
  echo "✅ Cluster is up"
else
  echo "❌ Cluster is not reachable"
fi
