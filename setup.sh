#!/bin/sh

# Check if ENV environment variable equals "local"
if [ "$ENV" = "local" ]; then
    echo "ENV is set to 'local', starting kind cluster..."
    # Start the kind cluster using the existing script
    ./kind/start-cluster.sh
else
    echo "ENV is not set to 'local' (current value: '$ENV'), skipping kind cluster setup"
fi

if kubectl version --short >/dev/null 2>&1; then
  echo "✅ Cluster is up"
else
  echo "❌ Cluster is not reachable"
fi
