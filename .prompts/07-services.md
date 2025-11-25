# Services Configuration

This prompt covers the application services deployed on the Darwin platform.

---

## Available Services

| Service | Type | Port | Description |
|---------|------|------|-------------|
| `feature-store` | Java | 8080 | Feature Store API |
| `feature-store-admin` | Java | 8080 | Feature Store Admin API |
| `feature-store-consumer` | Java | 8080 | Feature Store Consumer |
| `compute` | Python | 8000 | Compute orchestration |
| `cluster-manager` | Go | 8080 | Kubernetes cluster management |
| `mlflow-lib` | Python | 8080 | MLflow library service |
| `mlflow-app` | Python | 8000 | MLflow application |
| `workspace` | Python | 8000 | User workspace service |
| `chronos` | Python | 8000 | Scheduling service |
| `chronos-consumer` | Python | 8000 | Chronos event consumer |

---

## Services Configuration File

**File**: `helm/darwin/charts/services/values.yaml`

```yaml
services:
  compute:
    enabled: true
    replicaCount: 1
    serviceName: darwin-compute
    image:
      registry: localhost:5000
      name: darwin-compute
      tag: latest
      pullPolicy: Always
    service:
      type: ClusterIP
      port: 8000
    ingress:
      enabled: false
    healthcheck:
      path: /health
      initialDelaySeconds: 30
      readinessDelay: 15
    autoscaling:
      enabled: false
      minReplicas: 1
      maxReplicas: 5
      targetCPUUtilizationPercentage: 80
    resources:
      limits:
        cpu: 500m
        memory: 512Mi
      requests:
        cpu: 100m
        memory: 256Mi
    extraEnvVars:
      - name: LOG_LEVEL
        value: "INFO"
```

---

## Service Configuration Options

### Basic Settings
```yaml
serviceName: darwin-compute    # Internal service name
enabled: true                  # Enable deployment
replicaCount: 1                # Number of replicas
```

### Image Configuration
```yaml
image:
  registry: localhost:5000     # Docker registry
  name: darwin-compute         # Image name (matches services.yaml)
  tag: latest                  # Image tag
  pullPolicy: Always           # IfNotPresent, Always, Never
```

### Service & Networking
```yaml
service:
  type: ClusterIP              # ClusterIP, NodePort, LoadBalancer
  port: 8000                   # Service port
```

### Health Checks
```yaml
healthcheck:
  path: /health                # Health endpoint path
  initialDelaySeconds: 30      # Wait before first check
  readinessDelay: 15           # Wait before readiness probe
```

### Resources
```yaml
resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 100m
    memory: 256Mi
```

### Autoscaling
```yaml
autoscaling:
  enabled: true
  minReplicas: 1
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
```

### Environment Variables
```yaml
extraEnvVars:
  - name: LOG_LEVEL
    value: "DEBUG"
  - name: CUSTOM_CONFIG
    value: "value"
```

### Volumes (Optional)
```yaml
volumeMounts:
  - name: shared-data
    mountPath: /var/www/fsx
    subPath: workspace
volumes:
  - name: shared-data
    persistentVolumeClaim:
      claimName: darwin-shared-pvc
```

---

## Currently Enabled Services

Check `services.yaml` and Helm values for currently enabled services:

### In services.yaml (Build)
```yaml
applications:
  - application: darwin-compute
    enabled: true              # Will build this image
```

### In Helm values (Deploy)
```yaml
services:
  compute:
    enabled: true              # Will deploy this service
```

**Both must be enabled** for a service to be built and deployed.

---

## Environment Variables (Auto-Injected)

All services automatically receive these environment variables:

```yaml
# Team/Environment
- name: TEAM_SUFFIX
  value: "-darwin-local"
- name: VPC_SUFFIX
  value: "-darwin-local"
- name: ENV
  value: "darwin-local"
- name: APP_DIR
  value: "/app"
- name: DEPLOYMENT_TYPE
  value: "container"

# Database Connections
- name: DARWIN_MYSQL_HOST
  value: "darwin-mysql"
- name: DARWIN_MYSQL_USERNAME
  value: "root"
- name: DARWIN_MYSQL_PASSWORD
  value: "password"
- name: DARWIN_CASSANDRA_HOST
  value: "darwin-cassandra"
- name: DARWIN_KAFKA_HOST
  value: "darwin-kafka"
- name: DARWIN_ZOOKEEPER_HOST
  value: "darwin-zookeeper"

# Service Discovery
- name: DARWIN_FEATURE_STORE_HOST
  value: "darwin-feature-store"
- name: DARWIN_FEATURE_STORE_ADMIN_HOST
  value: "darwin-feature-store-admin"

# AWS/LocalStack
- name: AWS_ENDPOINT_OVERRIDE
  value: "http://darwin-localstack:4566"
```

---

## Ingress Configuration

### Centralized Ingress
**File**: `helm/darwin/charts/services/values.yaml`

```yaml
ingress:
  enabled: true
  className: "nginx"
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
    nginx.ingress.kubernetes.io/use-regex: "true"
  host: "localhost"
  paths:
    - path: "/feature-store(/|$)(.*)"
      service: "feature-store"
      port: 8080
    - path: "/compute(/|$)(.*)"
      service: "compute"
      port: 8000
    - path: "/cluster-manager(/|$)(.*)"
      service: "cluster-manager"
      port: 8080
    - path: "/mlflow-lib(/|$)(.*)"
      service: "mlflow-lib"
      port: 8080
    - path: "/mlflow-app(/|$)(.*)"
      service: "mlflow-app"
      port: 8000
    - path: "/workspace(/|$)(.*)"
      service: "workspace"
      port: 8000
```

### Per-Service Ingress (Alternative)
```yaml
services:
  my-service:
    ingress:
      enabled: true
      className: nginx
      annotations:
        nginx.ingress.kubernetes.io/rewrite-target: /$2
      hosts:
        - host: localhost
          paths:
            - path: /my-service(/|$)(.*)
              pathType: ImplementationSpecific
```

---

## Service URL Patterns

### Internal (Pod-to-Pod)
```
http://darwin-{service-name}:{port}

# Examples:
http://darwin-compute:8000
http://darwin-mysql:3306
http://darwin-feature-store:8080
```

### External (Via Ingress)
```
http://localhost/{service-path}/

# Examples:
http://localhost/compute/health
http://localhost/feature-store/api/v1/features
http://localhost/mlflow-app/experiments
```

---

## Service-Specific Configurations

### Compute Service
```yaml
compute:
  enabled: true
  extraEnvVars:
    - name: VAULT_SERVICE_MYSQL_USERNAME
      value: "root"
    - name: VAULT_SERVICE_MYSQL_PASSWORD
      value: "password"
    - name: CONFIG_SERVICE_MYSQL_DATABASE
      value: "darwin"
    - name: OTEL_DISABLED
      value: "true"
```

### Cluster Manager
```yaml
cluster-manager:
  enabled: true
  extraEnvVars:
    - name: ARTIFACT_VERSION
      value: "1.0.0"
    - name: AWS_ENDPOINT_URL_S3
      value: "http://darwin-localstack:4566"
```

### MLflow Services
```yaml
mlflow-lib:
  enabled: true
  extraEnvVars:
    - name: CONFIG_SERVICE_MYSQL_MASTERHOST
      value: "darwin-mysql"
    - name: CONFIG_SERVICE_MYSQL_DATABASE
      value: "darwin_mlflow"
    - name: MLFLOW_S3_BUCKET
      value: "darwin-mlflow"

mlflow-app:
  enabled: true
  extraEnvVars:
    - name: MLFLOW_UI_URL
      value: "http://darwin-mlflow-lib:8080"
    - name: MLFLOW_APP_LAYER_URL
      value: "http://darwin-mlflow-lib:8080"
```

---

## Enable/Disable Services

### Enable a Service
```bash
# Via Helm command
helm upgrade darwin ./helm/darwin \
  --set services.compute.enabled=true

# Or edit values.yaml
services:
  compute:
    enabled: true
```

### Scale a Service
```bash
# Via Helm
helm upgrade darwin ./helm/darwin \
  --set services.compute.replicaCount=3

# Via kubectl
kubectl scale deployment darwin-compute -n darwin --replicas=3
```

---

## Service Health Checks

### Check All Services
```bash
kubectl get pods -n darwin -l darwin-component-type=service
```

### Check Specific Service
```bash
# Pod status
kubectl get pods -n darwin | grep compute

# Logs
kubectl logs -n darwin deployment/darwin-compute -f

# Health endpoint
kubectl exec -n darwin deployment/darwin-compute -- \
  curl -s localhost:8000/health
```

### Port Forward for Testing
```bash
kubectl port-forward -n darwin svc/darwin-compute 8000:8000
curl http://localhost:8000/health
```

---

## RBAC

Services use a shared service account with permissions to:
- Read secrets
- Access Kubernetes API (for cluster-manager)
- Create pods (for compute)

**File**: `helm/darwin/charts/services/templates/services-rbac.yaml`

---

## Shared Storage

Services can mount shared storage:

```yaml
storage:
  enabled: true
  storageClassName: shared-services
  size: 5Gi
  hostPath: /mnt/shared-data/efs
```

Access via `darwin-shared-pvc` PVC.

---

## Related Prompts

- **Adding services**: `.prompts/03-add-service.md`
- **Helm charts**: `.prompts/05-helm-charts.md`
- **Datastores**: `.prompts/06-datastores.md`
- **Troubleshooting**: `.prompts/08-troubleshooting.md`

