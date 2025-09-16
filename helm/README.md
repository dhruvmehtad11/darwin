# Darwin Platform Helm Charts

This directory contains Helm charts for deploying the complete Darwin Platform stack on Kubernetes.

## Overview

The Darwin Platform is a unified ecosystem for machine learning workflows, feature engineering, and compute orchestration, consisting of:

### **Platform Components:**
- **Darwin Feature Store**: Feature engineering and serving (darwin-ofs-v2)
- **Darwin Workflow**: Orchestration and pipeline management
- **Darwin Compute**: Distributed compute and job scheduling

### **Data Infrastructure:**
- **MySQL**: Metadata and configuration storage
- **Cassandra**: High-performance feature storage
- **Kafka**: Real-time data streaming
- **Zookeeper**: Kafka coordination
- **Airflow**: Workflow orchestration

## Prerequisites

- Kubernetes 1.20+
- Helm 3.8+
- StorageClass configured for persistent volumes
- Ingress controller (NGINX recommended)

## Quick Start

### 1. Install the Complete Stack

```bash
# Add the Darwin Helm repository (if using external repo)
helm repo add darwin https://your-helm-repo.com/darwin
helm repo update

# Install the complete Darwin stack
helm install darwin ./helm/darwin \
  --namespace darwin-feature-store \
  --create-namespace \
  --values ./helm/darwin/values.yaml
```

### 2. Install Individual Components

```bash
# Install only MySQL
helm install mysql ./helm/darwin/charts/datastores/mysql \
  --namespace darwin-feature-store

# Install only the application
helm install darwin-app ./helm/darwin/charts/apps/darwin-ofs-v2 \
  --namespace darwin-feature-store
```

## Configuration

### Global Configuration

Edit `helm/darwin/values.yaml` to customize the deployment:

```yaml
global:
  imageRegistry: docker.io
  storageClass: standard
  namespace: darwin-feature-store

# Enable/disable components
darwinOfsV2:
  enabled: true
mysql:
  enabled: true
cassandra:
  enabled: true
kafka:
  enabled: true
zookeeper:
  enabled: true
airflow:
  enabled: true
```

### Environment-Specific Values

Create environment-specific values files:

```bash
# Development
helm install darwin ./helm/darwin -f values-dev.yaml

# Production
helm install darwin ./helm/darwin -f values-prod.yaml
```

## Component Details

### Darwin Platform Components

#### **Feature Store Component**
The Darwin Feature Store provides:
- REST API for feature management
- Real-time feature serving
- Feature validation and monitoring
- Integration with ML pipelines

**Key Configuration:**
```yaml
darwinFeatureStore:
  enabled: true
  replicaCount: 2
  resources:
    requests:
      cpu: 500m
      memory: 2Gi
  autoscaling:
    enabled: true
    maxReplicas: 10
```

#### **Workflow Component**
Darwin Workflow Engine provides:
- Pipeline orchestration
- Task scheduling and management
- Workflow monitoring and alerting
- Integration with compute resources

**Key Configuration:**
```yaml
darwinWorkflow:
  enabled: false  # Enable when needed
  replicaCount: 1
  service:
    port: 8081
```

#### **Compute Component**
Darwin Compute Engine provides:
- Distributed job scheduling
- Resource allocation and management
- Compute cluster orchestration
- Scaling and optimization

**Key Configuration:**
```yaml
darwinCompute:
  enabled: false  # Enable when needed
  replicaCount: 1
  service:
    port: 8082
```

### MySQL Database

Stores metadata, configurations, and feature definitions.

**Key Configuration:**
```yaml
mysql:
  auth:
    database: darwin_ofs
    username: darwin_user
  primary:
    persistence:
      size: 20Gi
```

### Cassandra Database

High-performance storage for feature values.

**Key Configuration:**
```yaml
cassandra:
  cluster:
    replicaCount: 3
  persistence:
    size: 50Gi
```

### Kafka Streaming

Real-time feature streaming and event processing.

**Key Configuration:**
```yaml
kafka:
  replicaCount: 3
  topics:
    - name: feature-updates
      partitions: 6
```

### Airflow Orchestration

Workflow orchestration for feature pipelines.

**Key Configuration:**
```yaml
airflow:
  executor: CeleryExecutor
  workers:
    replicas: 2
```

## Access Points

After installation, the following services will be available:

| Service | URL | Purpose |
|---------|-----|---------|
| Darwin Feature Store | http://darwin-feature-store.local | Feature API |
| Darwin Workflow | http://darwin-workflow.local | Workflow Engine |
| Darwin Compute | http://darwin-compute.local | Compute Engine |
| Airflow | http://airflow.darwin.local | Workflow UI |
| MySQL | mysql:3306 | Database |
| Cassandra | cassandra:9042 | Data Storage |
| Kafka | kafka:9092 | Streaming |

## Monitoring

The charts include monitoring components:

```yaml
monitoring:
  enabled: true
  prometheus:
    enabled: true
  grafana:
    enabled: true
```

Access Grafana at: http://grafana.darwin.local
- Username: admin
- Password: admin123

## Scaling

### Horizontal Scaling

```bash
# Scale Darwin application
kubectl scale deployment darwin-ofs-v2 --replicas=5

# Scale Cassandra cluster
helm upgrade darwin ./helm/darwin \
  --set cassandra.cluster.replicaCount=5
```

### Vertical Scaling

```yaml
# Update values.yaml
darwinOfsV2:
  resources:
    requests:
      cpu: 1000m
      memory: 4Gi
    limits:
      cpu: 2000m
      memory: 8Gi
```

## Backup and Recovery

### Database Backups

```bash
# MySQL backup
kubectl exec -it mysql-0 -- mysqldump -u root -p darwin_ofs > backup.sql

# Cassandra backup
kubectl exec -it cassandra-0 -- nodetool snapshot darwin_ofs
```

### Persistent Volume Backups

Follow your cloud provider's backup solutions for persistent volumes.

## Troubleshooting

### Common Issues

1. **Pods stuck in Pending**: Check storage class and PVC creation
2. **Database connection errors**: Verify credentials and network policies
3. **Ingress not working**: Check ingress controller and DNS configuration

### Debug Commands

```bash
# Check pod status
kubectl get pods -n darwin-feature-store

# View logs
kubectl logs -f deployment/darwin-ofs-v2 -n darwin-feature-store

# Check services
kubectl get svc -n darwin-feature-store

# Describe problematic pods
kubectl describe pod <pod-name> -n darwin-feature-store
```

## Uninstalling

```bash
# Uninstall the complete stack
helm uninstall darwin -n darwin-feature-store

# Remove namespace (this will delete all data!)
kubectl delete namespace darwin-feature-store
```

## Development

### Local Development

For local development with Kind:

```bash
# Start local cluster
ENV=local ./setup.sh

# Install with development values
helm install darwin ./helm/darwin \
  -f values-dev.yaml \
  --namespace darwin-feature-store \
  --create-namespace
```

### Chart Development

```bash
# Lint charts
helm lint ./helm/darwin

# Template and check output
helm template darwin ./helm/darwin --debug

# Test installation
helm install darwin ./helm/darwin --dry-run --debug
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make changes to charts
4. Test with `helm lint` and `helm template`
5. Submit a pull request

## Support

For support and issues:
- GitHub: https://github.com/dream11/darwin-ofs-v2
- Email: infraautomation@dream11.com
- Docs: https://docs.darwin.dream11.com

## License

Apache License 2.0
