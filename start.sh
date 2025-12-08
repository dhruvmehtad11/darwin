#!/bin/sh
set -e

# Check for init configuration
ENABLED_SERVICES_FILE=".setup/enabled-services.yaml"
if [ ! -f "$ENABLED_SERVICES_FILE" ]; then
    echo "❌ No configuration found at $ENABLED_SERVICES_FILE"
    echo "   Please run ./init.sh first to configure which services to enable."
    exit 1
fi
echo "✅ Found configuration: $ENABLED_SERVICES_FILE"

# ============================================================================
# CLI TOOLS SETUP
# ============================================================================
# Check if hermes-cli is enabled and install if needed
HERMES_CLI_ENABLED=$(yq eval '.cli-tools.hermes-cli // false' "$ENABLED_SERVICES_FILE" 2>/dev/null || echo "false")

if [ "$HERMES_CLI_ENABLED" = "true" ]; then
  echo ""
  echo "📦 Setting up hermes-cli..."
  
  HERMES_CLI_PATH="hermes-cli"
  if [ ! -d "$HERMES_CLI_PATH" ]; then
    echo "   ⚠️  hermes-cli directory not found at $HERMES_CLI_PATH, skipping..."
  else
    VENV_PATH="$HERMES_CLI_PATH/.venv"
    
    # Create venv if it doesn't exist
    if [ ! -d "$VENV_PATH" ]; then
      echo "   Creating virtual environment..."
      python3.9 -m venv "$VENV_PATH"
    fi

    # Install hermes-cli
    echo "   Installing hermes-cli package..."
    (
      cd "$HERMES_CLI_PATH" && source .venv/bin/activate && pip install -e . --force-reinstall --no-cache-dir
    )

    if [ $? -eq 0 ]; then
      echo "   ✅ hermes-cli installed successfully"
      echo "   To use: source $HERMES_CLI_PATH/.venv/bin/activate"
    else
      echo "   ❌ Failed to install hermes-cli"
    fi
  fi
  echo ""
fi

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

echo "⚙️  Setting up Kubernetes dependencies..."
./k8s-setup.sh

# ============================================================================
# COPY AIRFLOW SOURCE TO KIND NODES (if airflow is enabled and using kind)
# ============================================================================
copy_airflow_source_to_kind() {
  # Check if airflow is enabled
  AIRFLOW_ENABLED=$(yq eval '.datastores.airflow // false' "$ENABLED_SERVICES_FILE" 2>/dev/null || echo "false")
  
  if [ "$AIRFLOW_ENABLED" != "true" ]; then
    return 0  # Airflow not enabled, skip
  fi
  
  # Check if we're using kind cluster
  if ! echo "$KUBECONFIG" | grep -q "kind"; then
    return 0  # Not using kind, skip
  fi
  
  # Check if airflow source directory exists
  if [ ! -d "workflow/airflow" ]; then
    echo "   ⚠️  workflow/airflow directory not found, skipping airflow source copy"
    return 0
  fi
  
  echo "📦 Copying airflow source code to kind nodes..."
  
  # Get all kind nodes
  KIND_NODES=$(docker ps --format '{{.Names}}' | grep '^kind-' || true)
  
  if [ -z "$KIND_NODES" ]; then
    echo "   ⚠️  No kind nodes found, skipping airflow source copy"
    return 0
  fi
  
  # Copy to each kind node
  for node in $KIND_NODES; do
    echo "   Copying to $node..."
    # Copy airflow source
    if cd workflow/airflow && tar czf - . 2>/dev/null | docker exec -i "$node" sh -c "mkdir -p /tmp/workflow/airflow && cd /tmp/workflow/airflow && tar xzf -" 2>/dev/null; then
      echo "   ✅ Successfully copied airflow to $node"
    else
      echo "   ⚠️  Failed to copy airflow to $node (may already exist)"
    fi
    cd ../..
    # Copy commons module
    if cd workflow/commons && tar czf - . 2>/dev/null | docker exec -i "$node" sh -c "mkdir -p /tmp/workflow/commons && cd /tmp/workflow/commons && tar xzf -" 2>/dev/null; then
      echo "   ✅ Successfully copied commons to $node"
    else
      echo "   ⚠️  Failed to copy commons to $node (may already exist)"
    fi
    cd ../..
    # Copy darwin-compute SDK from the built target directory
    if cd workflow/target/darwin-workflow/darwin-compute/sdk && tar czf - . 2>/dev/null | docker exec -i "$node" sh -c "mkdir -p /tmp/workflow/darwin-compute-sdk && cd /tmp/workflow/darwin-compute-sdk && tar xzf -" 2>/dev/null; then
      echo "   ✅ Successfully copied darwin-compute-sdk to $node"
    else
      echo "   ⚠️  Failed to copy darwin-compute-sdk to $node (may already exist)"
    fi
    cd ../../../../..
    # Copy darwin-compute model from the built target directory
    if cd workflow/target/darwin-workflow/darwin-compute/model && tar czf - . 2>/dev/null | docker exec -i "$node" sh -c "mkdir -p /tmp/workflow/darwin-compute-model && cd /tmp/workflow/darwin-compute-model && tar xzf -" 2>/dev/null; then
      echo "   ✅ Successfully copied darwin-compute-model to $node"
    else
      echo "   ⚠️  Failed to copy darwin-compute-model to $node (may already exist)"
    fi
    cd ../../../../..
    # Copy darwin-compute core from the built target directory
    if cd workflow/target/darwin-workflow/darwin-compute/core && tar czf - . 2>/dev/null | docker exec -i "$node" sh -c "mkdir -p /tmp/workflow/darwin-compute-core && cd /tmp/workflow/darwin-compute-core && tar xzf -" 2>/dev/null; then
      echo "   ✅ Successfully copied darwin-compute-core to $node"
    else
      echo "   ⚠️  Failed to copy darwin-compute-core to $node (may already exist)"
    fi
    cd ../../../../..
  done
  
  echo "   ✅ Airflow source code copied to kind nodes"
}

# Copy airflow source if needed
copy_airflow_source_to_kind

echo "🚀 Starting Darwin Platform deployment..."

# ============================================================================
# BUILD HELM OVERRIDES FROM CONFIG
# ============================================================================
echo "📋 Reading service configuration..."

HELM_OVERRIDES=""

# Function to map application name to helm path
get_helm_path() {
  local app_name="$1"
  case "$app_name" in
    "darwin-ofs-v2") echo "services.services.feature-store.enabled" ;;
    "darwin-ofs-v2-admin") echo "services.services.feature-store-admin.enabled" ;;
    "darwin-ofs-v2-consumer") echo "services.services.feature-store-consumer.enabled" ;;
    "darwin-mlflow") echo "services.services.mlflow-lib.enabled" ;;
    "darwin-mlflow-app") echo "services.services.mlflow-app.enabled" ;;
    "chronos") echo "services.services.chronos.enabled" ;;
    "chronos-consumer") echo "services.services.chronos-consumer.enabled" ;;
    "darwin-compute") echo "services.services.compute.enabled" ;;
    "darwin-cluster-manager") echo "services.services.cluster-manager.enabled" ;;
    "darwin-workspace") echo "services.services.workspace.enabled" ;;
    "ml-serve-app") echo "services.services.ml-serve-app.enabled" ;;
    "artifact-builder") echo "services.services.artifact-builder.enabled" ;;
    "darwin-catalog") echo "services.services.catalog.enabled" ;;
    "darwin-workflow") echo "services.services.workflow.enabled" ;;
    *) echo "" ;;
  esac
}

# Read applications from config and build --set flags
echo "   Processing applications..."
for app_name in $(yq eval '.applications | keys | .[]' "$ENABLED_SERVICES_FILE"); do
  enabled=$(yq eval ".applications.\"$app_name\"" "$ENABLED_SERVICES_FILE")
  helm_path=$(get_helm_path "$app_name")
  
  if [ -n "$helm_path" ]; then
    HELM_OVERRIDES="$HELM_OVERRIDES --set $helm_path=$enabled"
    echo "     $app_name -> $helm_path=$enabled"
  fi
done

# Read datastores from config and build --set flags (direct mapping)
echo "   Processing datastores..."
for ds_name in $(yq eval '.datastores | keys | .[]' "$ENABLED_SERVICES_FILE"); do
  enabled=$(yq eval ".datastores.\"$ds_name\"" "$ENABLED_SERVICES_FILE")
  
  # Skip busybox - it's not a helm-managed datastore
  if [ "$ds_name" = "busybox" ]; then
    continue
  fi
  
  helm_path="datastores.$ds_name.enabled"
  HELM_OVERRIDES="$HELM_OVERRIDES --set $helm_path=$enabled"
  echo "     $ds_name -> $helm_path=$enabled"
done

echo ""
echo "📦 Installing Darwin Platform with configuration overrides..."

# Configure local registry prefix for images when using Kind
LOCAL_REGISTRY_PREFIX=""
if echo "$KUBECONFIG" | grep -q "kind"; then
  # The containerd mirror in kind-config.yaml maps localhost:5000 to kind-registry:5000
  # Inside the cluster, the registry is always accessible as kind-registry:5000
  # The host port (from DOCKER_REGISTRY) is dynamic, but we use localhost:5000 for images
  # because that's what the containerd mirror expects
  LOCAL_REGISTRY_PREFIX="localhost:5000/"
  echo "   Using local registry prefix: $LOCAL_REGISTRY_PREFIX"
  echo "   (Registry host port: ${DOCKER_REGISTRY:-unknown}, cluster uses kind-registry:5000 via mirror)"
  
  # Set global image registry (if templates support it)
  HELM_OVERRIDES="$HELM_OVERRIDES --set global.imageRegistry=$LOCAL_REGISTRY_PREFIX"
  
  # Override specific image repositories to use local registry
  HELM_OVERRIDES="$HELM_OVERRIDES --set datastores.airflow.image.repository=${LOCAL_REGISTRY_PREFIX}apache/airflow"
  HELM_OVERRIDES="$HELM_OVERRIDES --set datastores.localstack.image.repository=${LOCAL_REGISTRY_PREFIX}localstack/localstack"
  
  # Note: busybox images now use the darwin.image helper which respects global.imageRegistry
fi

# Install Darwin Platform umbrella chart with overrides
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin \
  --create-namespace \
  --wait \
  --timeout 600s \
  $HELM_OVERRIDES

echo "✅ Deployment completed!"

# Show hermes-cli activation reminder if it was installed
HERMES_CLI_ENABLED=$(yq eval '.cli-tools.hermes-cli // false' "$ENABLED_SERVICES_FILE" 2>/dev/null || echo "false")
if [ "$HERMES_CLI_ENABLED" = "true" ]; then
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  echo "📦 To use hermes-cli, activate the virtual environment:"
  echo ""
  echo "   source hermes-cli/.venv/bin/activate"
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
fi
