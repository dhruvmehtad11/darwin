#!/bin/sh
set -e

CLUSTER_NAME=$CLUSTER_NAME
KUBECONFIG=$KUBECONFIG

if ! command -v kind &> /dev/null; then
    echo "❌ kind could not be found, please install it first"
    exit 1
fi

# Check if cluster exists
if kind get clusters | grep -q "^${CLUSTER_NAME}$"; then
  echo "🛑 Stopping kind cluster '${CLUSTER_NAME}'..."
  
  # Delete the kind cluster
  kind delete cluster --name "${CLUSTER_NAME}"
  
  # Clean up kubeconfig file if it exists
  if [ -f "${KUBECONFIG}" ]; then
    echo "🧹 Cleaning up kubeconfig file: ${KUBECONFIG}"
    rm -f "${KUBECONFIG}"
  fi
  
  echo "✅ Kind cluster '${CLUSTER_NAME}' has been successfully stopped and removed"
else
  echo "ℹ️  Kind cluster '${CLUSTER_NAME}' does not exist or is already stopped"
fi


if docker ps | grep -q "kind-registry"; then
  echo "🛑 Stopping kind-registry..."
  docker stop kind-registry
  docker rm kind-registry
fi

# Unset the KUBECONFIG environment variable
unset KUBECONFIG
echo "🔧 KUBECONFIG environment variable has been unset"
