#!/bin/sh
set -e


CLUSTER_NAME=kind  # change if you use --name
CONFIG=./kind/kind-config.yaml
KUBECONFIG=./kind/config/kindkubeconfig.yaml

if ! command -v kind &> /dev/null; then
    echo "kind could not be found, installing it"
    brew install kind || apt-get install kind
fi

export KUBECONFIG=./kind/config/kindkubeconfig.yaml

# Check if cluster exists
if kind get clusters | grep -q "^${CLUSTER_NAME}$"; then
  echo "✅ kind cluster '${CLUSTER_NAME}' already exists"
else
  echo "🚀 Creating kind cluster '${CLUSTER_NAME}'..."
  kind create cluster \
    --name "${CLUSTER_NAME}" \
    --config "${CONFIG}" \
    --kubeconfig "${KUBECONFIG}"

  # Install cert-manager with CRDs
  helm install cert-manager jetstack/cert-manager \
    --namespace cert-manager \
    --create-namespace \
    --set installCRDs=true

  # Wait for cert-manager pods to be ready
  kubectl wait --for=condition=Available --timeout=120s deployment/cert-manager -n cert-manager

  kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/kind/deploy.yaml
  kubectl label node kind-control-plane ingress-ready=true
fi
