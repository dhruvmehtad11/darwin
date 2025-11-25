# Deployment Process

This prompt covers deploying the Darwin platform to Kubernetes after images are built.

---

## Prerequisites

Before running `start.sh`:

1. Run `init.sh` to configure services (creates `.setup/enabled-services.yaml`)
2. Run `setup.sh` to build images

```bash
sh init.sh      # One-time: select services
sh setup.sh     # Build images
sh start.sh     # Deploy
```

---

## Deployment Overview

```
start.sh
    │
    ├── Check .setup/enabled-services.yaml exists
    ├── Source config.env
    ├── Verify cluster connectivity
    │
    ├── k8s-setup.sh
    │   ├── Install kuberay-operator (from local registry)
    │   ├── Install nginx-proxy-server (from local registry)
    │   ├── Install kube-prometheus-stack (from local registry)
    │   └── Create RBAC & PVs
    │
    └── helm upgrade --install darwin ./helm/darwin
        ├── Build --set overrides from .setup/enabled-services.yaml
        │   ├── --set services.services.compute.enabled=true
        │   ├── --set services.services.mlflow-lib.enabled=false
        │   ├── --set datastores.mysql.enabled=true
        │   └── ... (for each service/datastore)
        ├── Namespace: darwin
        ├── Datastores subchart (only enabled ones)
        │   └── MySQL, LocalStack, OpenSearch, etc.
        ├── Datastore health check job
        └── Services subchart (only enabled ones)
            └── Feature Store, MLflow, Compute, etc.
```

---

## Entry Point: start.sh

**Location**: `start.sh`

```bash
#!/bin/sh
set -e

# Check for init configuration
ENABLED_SERVICES_FILE=".setup/enabled-services.yaml"
if [ ! -f "$ENABLED_SERVICES_FILE" ]; then
    echo "❌ No configuration found at $ENABLED_SERVICES_FILE"
    echo "   Please run ./init.sh first to configure which services to enable."
    exit 1
fi

# Source configuration
set -o allexport
. config.env
set +o allexport

# Verify cluster
kubectl version >/dev/null 2>&1 || exit 1

# Setup K8s infrastructure
./k8s-setup.sh

# Build helm overrides from config
HELM_OVERRIDES=""
# ... reads .setup/enabled-services.yaml and builds --set flags

# Deploy Darwin platform with config-based overrides
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin \
  --create-namespace \
  --wait \
  --timeout 600s \
  $HELM_OVERRIDES
```

---

## Configuration-Based Deployment

`start.sh` reads `.setup/enabled-services.yaml` and maps selections to Helm values:

### Application Mapping

| Config Key | Helm Path |
|------------|-----------|
| `darwin-ofs-v2` | `services.services.feature-store.enabled` |
| `darwin-ofs-v2-admin` | `services.services.feature-store-admin.enabled` |
| `darwin-ofs-v2-consumer` | `services.services.feature-store-consumer.enabled` |
| `darwin-mlflow` | `services.services.mlflow-lib.enabled` |
| `darwin-mlflow-app` | `services.services.mlflow-app.enabled` |
| `chronos` | `services.services.chronos.enabled` |
| `chronos-consumer` | `services.services.chronos-consumer.enabled` |
| `darwin-compute` | `services.services.compute.enabled` |
| `darwin-cluster-manager` | `services.services.cluster-manager.enabled` |
| `darwin-workspace` | `services.services.workspace.enabled` |

### Datastore Mapping

| Config Key | Helm Path |
|------------|-----------|
| `mysql` | `datastores.mysql.enabled` |
| `cassandra` | `datastores.cassandra.enabled` |
| `kafka` | `datastores.kafka.enabled` |
| `zookeeper` | `datastores.zookeeper.enabled` |
| `airflow` | `datastores.airflow.enabled` |
| `localstack` | `datastores.localstack.enabled` |
| `opensearch` | `datastores.opensearch.enabled` |

---

## Kubernetes Infrastructure: k8s-setup.sh

**Location**: `k8s-setup.sh`

**What it installs** (all using images from local registry):

### 1. Kuberay Operator (v1.1.0)
For managing Ray clusters (distributed ML workloads):
```bash
helm repo add kuberay https://ray-project.github.io/kuberay-helm/
helm upgrade --install kuberay-operator kuberay/kuberay-operator \
  --version 1.1.0 -n ray-system --create-namespace \
  --set image.repository=localhost:5000/quay.io/kuberay/operator \
  --set image.tag=v1.1.0
```

### 2. Nginx Proxy Server
Custom proxy for Ray services:
```bash
helm upgrade --install nginx-proxy-server ./helm/nginx-proxy-server \
  -n ray-system --create-namespace \
  --set nginx.image.repository=localhost:5000/nginx \
  --set nginx.image.tag=1.27.4
```

### 3. Kube Prometheus Stack
Monitoring with Prometheus + Grafana (grafana image from local registry):
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm upgrade --install kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --values ./helm/kube-prometheus-stack/values.yaml \
  -n prometheus-system --create-namespace
```

### 4. RBAC & Persistent Volumes
```bash
kubectl create namespace ray
kubectl create serviceaccount darwin-ds-role -n ray
helm upgrade --install pv-chart ./helm/pv-chart -n ray
```

---

## Helm Umbrella Chart

**Location**: `helm/darwin/`

**Chart.yaml dependencies**:
```yaml
dependencies:
  - name: datastores
    version: 1.0.0
    repository: "file://./charts/datastores"
    condition: datastores.enabled
  - name: services
    version: 1.0.0
    repository: "file://./charts/services"
    condition: services.enabled
```

### Deployment Order

1. **Datastores** deploy first (hook weight: -5)
2. **Health check job** verifies datastores are ready
3. **Services** deploy after health check passes (hook weight: 5)

---

## Main Values Configuration

**Location**: `helm/darwin/values.yaml`

```yaml
global:
  imageRegistry: docker.io
  storageClass: standard
  namespace: darwin
  local-k8s: true
  database:
    mysql:
      username: "root"
      rootPassword: "password"
      database: "darwin"
    cassandra:
      username: "darwin_user"
      password: "darwin_password"

datastores:
  enabled: true
  mysql:
    enabled: true      # Overridden by start.sh based on config
  cassandra:
    enabled: false
  kafka:
    enabled: false
  localstack:
    enabled: true

services:
  enabled: true
```

**Note**: The `enabled` values in `values.yaml` are defaults. `start.sh` overrides them with `--set` flags based on `.setup/enabled-services.yaml`.

---

## Deployment Commands

### Full Platform Deployment (Recommended)
```bash
sh init.sh              # Configure services (one-time)
sh setup.sh             # Build images
sh start.sh             # Deploy with config overrides
```

### Manual Deployment with Custom Overrides
```bash
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin --create-namespace \
  --set services.services.compute.enabled=true \
  --set services.services.mlflow-lib.enabled=false \
  --set datastores.mysql.enabled=true
```

### Deploy Only Datastores
```bash
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin --create-namespace \
  --set services.enabled=false
```

### Deploy Only Services
```bash
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin --create-namespace \
  --set datastores.enabled=false
```

### Upgrade Specific Service
```bash
helm upgrade darwin ./helm/darwin \
  --reuse-values \
  --set-string 'services.compute.image.tag=v2.0.0'
```

### Dry Run (Preview Changes)
```bash
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin --dry-run --debug
```

---

## Pre-Deploy Script (Optional)

**Location**: `pre-deploy.sh`

Runs database migrations before deployment:

```bash
# For each application in services.yaml:
run_pre_deploy_pod "darwin" "$image" "$application" "$extra_envs"
```

Creates a temporary pod that runs `.odin/pre-deploy.sh` with database connectivity.

---

## Service Endpoints After Deployment

| Service | Internal URL | External Path |
|---------|--------------|---------------|
| MySQL | `darwin-mysql:3306` | N/A |
| LocalStack | `darwin-localstack:4566` | N/A |
| Feature Store | `darwin-feature-store:8080` | `/feature-store/*` |
| MLflow Lib | `darwin-mlflow-lib:8080` | `/mlflow-lib/*` |
| MLflow App | `darwin-mlflow-app:8000` | `/mlflow-app/*` |
| Compute | `darwin-compute:8000` | `/compute/*` |
| Cluster Manager | `darwin-cluster-manager:8080` | `/cluster-manager/*` |

---

## Monitoring Deployment

### Check Pod Status
```bash
kubectl get pods -n darwin
kubectl get pods -n ray-system
kubectl get pods -n prometheus-system
```

### Watch Deployment Progress
```bash
kubectl get pods -n darwin -w
```

### Check Health Check Job
```bash
kubectl logs -n darwin jobs/darwin-datastore-health-check
```

### View Service Logs
```bash
kubectl logs -n darwin deployment/darwin-compute -f
```

### Check Ingress Routes
```bash
kubectl get ingress -n darwin
```

---

## Uninstall

### Remove Darwin Platform
```bash
helm uninstall darwin -n darwin
```

### Remove Infrastructure
```bash
helm uninstall kuberay-operator -n ray-system
helm uninstall nginx-proxy-server -n ray-system
helm uninstall kube-prometheus-stack -n prometheus-system
```

### Clean Up Namespaces
```bash
kubectl delete namespace darwin ray ray-system prometheus-system
```

---

## Troubleshooting Deployment

**Config not found**:
- Run `sh init.sh` to create `.setup/enabled-services.yaml`

**Services not starting**:
- Check datastore health: `kubectl logs jobs/darwin-datastore-health-check -n darwin`
- Verify datastores are running: `kubectl get pods -n darwin | grep -E "mysql|localstack"`
- Check if service is enabled in config: `cat .setup/enabled-services.yaml`

**Image pull errors**:
- Verify registry is accessible: `curl http://127.0.0.1:32768/v2/_catalog`
- Check image exists: `docker images | grep darwin`
- Ensure datastore/operator images were pushed: check setup.sh output

**Helm timeout**:
- Increase timeout: `--timeout 900s`
- Check pending pods: `kubectl describe pod <pod-name> -n darwin`

**Service enabled but not deployed**:
- Check helm override mapping in start.sh
- Verify the application name matches the mapping table above

---

## Related Prompts

- **Helm chart structure**: `.prompts/05-helm-charts.md`
- **Datastore configuration**: `.prompts/06-datastores.md`
- **Service configuration**: `.prompts/07-services.md`
- **Troubleshooting**: `.prompts/08-troubleshooting.md`

