#!/bin/sh
set -e

# Source the config.env file
set -o allexport
. config.env
set +o allexport

echo "🔧 Setting up KUBECONFIG: $KUBECONFIG"

# Verify cluster connectivity
if kubectl version >/dev/null 2>&1; then
  echo "✅ Cluster is accessible"
  kubectl get nodes
else
  echo "❌ Cluster is not reachable. Please run setup.sh first."
  exit 1
fi

echo "🚀 Starting Darwin Platform deployment..."

# Install Darwin Platform umbrella chart
echo "📦 Installing Darwin Platform..."
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin \
  --create-namespace \
  --wait \
  --timeout 600s

echo "✅ Deployment completed!"
