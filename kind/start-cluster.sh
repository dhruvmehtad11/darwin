#!/bin/sh
set -e

CLUSTER_NAME=$CLUSTER_NAME
KIND_CONFIG=$KIND_CONFIG
KUBECONFIG=$KUBECONFIG

if ! command -v kind &> /dev/null; then
    echo "kind could not be found, installing it"
    brew install kind || apt-get install kind
fi

export KUBECONFIG=$KUBECONFIG

# Check if cluster exists
if kind get clusters | grep -q "^${CLUSTER_NAME}$"; then
  echo "✅ kind cluster '${CLUSTER_NAME}' already exists"
else
  echo "🚀 Creating kind cluster '${CLUSTER_NAME}'..."
  
  kind create cluster \
    --name "${CLUSTER_NAME}" \
    --config "${KIND_CONFIG}" \
    --kubeconfig "${KUBECONFIG}"

  if docker ps | grep -q "kind-registry"; then
    echo "✅ kind-registry is already running"
  else
    echo "🚀 Starting kind-registry..."
    docker run -d --restart=always -p 5000:5000 --network $CLUSTER_NAME --name kind-registry registry:2
  fi

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
