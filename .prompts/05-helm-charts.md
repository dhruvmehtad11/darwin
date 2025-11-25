# Helm Charts Structure

The Darwin platform uses a Helm umbrella chart pattern with subcharts for datastores and services.

---

## Chart Hierarchy

```
helm/darwin/                    # Main umbrella chart
├── Chart.yaml                  # Chart metadata & dependencies
├── values.yaml                 # Global configuration
├── templates/                  # Umbrella-level templates
│   ├── _helpers.tpl
│   ├── configmap.yaml
│   ├── namespace.yaml
│   └── NOTES.txt
└── charts/
    ├── datastores/             # Databases subchart
    │   ├── Chart.yaml
    │   ├── values.yaml
    │   └── templates/
    │       ├── mysql-*.yaml
    │       ├── cassandra-*.yaml
    │       ├── kafka-*.yaml
    │       ├── localstack-*.yaml
    │       └── ...
    └── services/               # Applications subchart
        ├── Chart.yaml
        ├── values.yaml
        └── templates/
            ├── services.yaml   # Generic service deployment
            ├── ingress.yaml    # Centralized ingress
            └── ...
```

---

## Main Chart: helm/darwin/Chart.yaml

```yaml
apiVersion: v2
name: darwin
description: Darwin Platform Distribution
type: application
version: 1.0.0
appVersion: "2.0.0"

dependencies:
  - name: datastores
    version: 1.0.0
    repository: "file://./charts/datastores"
    condition: datastores.enabled
    tags:
      - databases
  - name: services
    version: 1.0.0
    repository: "file://./charts/services"
    condition: services.enabled
    tags:
      - applications
```

**Key points**:
- `condition`: Enable/disable subchart via `datastores.enabled`
- `repository`: Local file reference
- `tags`: Group related charts

---

## Global Values: helm/darwin/values.yaml

```yaml
global:
  imageRegistry: docker.io
  storageClass: standard
  namespace: darwin
  local-k8s: true              # Use hostPath vs PVC

  database:
    mysql:
      username: "root"
      rootPassword: "password"
      database: "darwin"
    cassandra:
      username: "darwin_user"
      password: "darwin_password"
    elasticsearch:
      username: "admin"
      password: "admin"

datastores:
  enabled: true
  mysql:
    enabled: true
  cassandra:
    enabled: false
  kafka:
    enabled: false
  localstack:
    enabled: true

services:
  enabled: true

monitoring:
  enabled: true
  prometheus:
    enabled: true
  grafana:
    enabled: true
```

Global values are inherited by all subcharts via `.Values.global`.

---

## Deployment Order

Helm hooks ensure correct startup sequence:

1. **Datastores** (hook weight: -5)
   - MySQL, Cassandra, Kafka deploy in parallel
   
2. **Health Check Job**
   - Waits for all datastores to be ready
   - Uses `nc -z` to check ports
   
3. **Services** (hook weight: 5)
   - Deploy after health check succeeds
   - Init containers also wait for datastores

---

## Datastores Subchart

**Location**: `helm/darwin/charts/datastores/`

### Structure
```
datastores/
├── Chart.yaml
├── values.yaml
└── templates/
    ├── mysql-deployment.yaml
    ├── mysql-service.yaml
    ├── mysql-pvc.yaml
    ├── mysql-init-configmap.yaml
    ├── cassandra-statefulset.yaml
    ├── cassandra-service.yaml
    ├── kafka-statefulset.yaml
    ├── kafka-service.yaml
    ├── zookeeper-statefulset.yaml
    ├── zookeeper-service.yaml
    ├── localstack-deployment.yaml
    ├── localstack-service.yaml
    ├── opensearch-statefulset.yaml
    └── opensearch-service.yaml
```

### Enable/Disable Datastores
```yaml
# In helm/darwin/values.yaml
datastores:
  enabled: true
  mysql:
    enabled: true
  cassandra:
    enabled: false
  kafka:
    enabled: false
  zookeeper:
    enabled: false
  localstack:
    enabled: true
  opensearch:
    enabled: true
```

---

## Services Subchart

**Location**: `helm/darwin/charts/services/`

### Structure
```
services/
├── Chart.yaml
├── values.yaml
└── templates/
    ├── _helpers.tpl
    ├── services.yaml           # Generic deployment template
    ├── ingress.yaml            # Centralized ingress
    ├── services-rbac.yaml      # RBAC for services
    ├── datastore-health-check.yaml
    └── shared-storage-pv.yaml
```

### Generic Service Template

The `services.yaml` template iterates over all services:

```yaml
{{- range $serviceName, $config := .Values.services }}
{{- if $config.enabled }}
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ $.Release.Name }}-{{ $serviceName }}
spec:
  replicas: {{ $config.replicaCount }}
  template:
    spec:
      initContainers:
      - name: wait-for-datastore-health
        # Waits for health check job to complete
      containers:
        - name: {{ $serviceName }}
          image: "{{ $config.image.registry }}/{{ $config.image.name }}:{{ $config.image.tag }}"
          env:
            # Common environment variables
            - name: DARWIN_MYSQL_HOST
              value: "{{ $.Release.Name }}-mysql"
            # Service-specific env vars
            {{- toYaml $config.extraEnvVars | nindent 12 }}
{{- end }}
{{- end }}
```

---

## Adding a New Service to Helm

### 1. Add to services/values.yaml
```yaml
services:
  my-new-service:
    enabled: true
    replicaCount: 1
    serviceName: my-new-service
    image:
      registry: localhost:5000
      name: my-new-service
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
    autoscaling:
      enabled: false
    resources:
      limits:
        cpu: 500m
        memory: 512Mi
      requests:
        cpu: 100m
        memory: 256Mi
    extraEnvVars:
      - name: MY_VAR
        value: "my-value"
```

### 2. Add Ingress Path (Optional)
```yaml
ingress:
  paths:
    - path: "/my-new-service(/|$)(.*)"
      service: "my-new-service"
      port: 8000
```

---

## Common Helm Operations

### Install
```bash
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin \
  --create-namespace \
  --wait
```

### Upgrade
```bash
helm upgrade darwin ./helm/darwin \
  --namespace darwin \
  --reuse-values
```

### Update Dependencies
```bash
cd helm/darwin
helm dependency update
```

### Dry Run
```bash
helm upgrade --install darwin ./helm/darwin \
  --namespace darwin \
  --dry-run --debug
```

### View Values
```bash
helm get values darwin -n darwin
```

### Template Output
```bash
helm template darwin ./helm/darwin --namespace darwin
```

---

## Overriding Values

### Via Command Line
```bash
helm upgrade darwin ./helm/darwin \
  --set services.compute.enabled=true \
  --set services.compute.replicaCount=2 \
  --set global.database.mysql.rootPassword=newpassword
```

### Via Values File
```bash
# Create custom-values.yaml
cat > custom-values.yaml <<EOF
services:
  compute:
    enabled: true
    replicaCount: 3
EOF

helm upgrade darwin ./helm/darwin -f custom-values.yaml
```

---

## Template Helpers

**Location**: `helm/darwin/templates/_helpers.tpl`

Common helpers used across templates:

```yaml
{{/* Chart labels */}}
{{- define "darwin.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* Selector labels */}}
{{- define "darwin.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
```

---

## Chart Testing

### Lint Chart
```bash
helm lint ./helm/darwin
```

### Test Installation
```bash
helm test darwin -n darwin
```

### Validate Templates
```bash
helm template darwin ./helm/darwin | kubectl apply --dry-run=client -f -
```

---

## File Reference

| File | Purpose |
|------|---------|
| `helm/darwin/Chart.yaml` | Main chart definition |
| `helm/darwin/values.yaml` | Global defaults |
| `helm/darwin/charts/datastores/values.yaml` | Database configs |
| `helm/darwin/charts/services/values.yaml` | Service configs |
| `helm/darwin/charts/services/templates/services.yaml` | Generic deployment |
| `helm/darwin/charts/services/templates/ingress.yaml` | Routing rules |

---

## Related Prompts

- **Datastores**: `.prompts/06-datastores.md`
- **Services**: `.prompts/07-services.md`
- **Deployment**: `.prompts/02-deployment.md`

