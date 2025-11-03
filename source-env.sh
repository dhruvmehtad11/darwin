#!/bin/bash
# Helper script to source config.env with proper path resolution

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Change to project root
cd "$SCRIPT_DIR"

# Source config.env if it exists
if [ -f "config.env" ]; then
  # Export variables from config.env, resolving relative paths
  export DOCKER_REGISTRY=$(grep "^DOCKER_REGISTRY=" config.env | cut -d'=' -f2-)
  
  # Resolve KUBECONFIG relative path to absolute path
  KUBECONFIG_REL=$(grep "^KUBECONFIG=" config.env | cut -d'=' -f2-)
  if [ -n "$KUBECONFIG_REL" ]; then
    export KUBECONFIG="$SCRIPT_DIR/$KUBECONFIG_REL"
    echo "✅ KUBECONFIG set to: $KUBECONFIG"
  fi
  
  echo "✅ Environment variables loaded from config.env"
  echo "You can now use kubectl commands."
else
  echo "❌ config.env not found"
  exit 1
fi

