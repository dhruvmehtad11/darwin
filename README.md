# Darwin Distribution

Complete Darwin Feature Store platform for local development and deployment.

## Quick Start

1. **Setup Cluster**: `sh setup.sh` - Creates Kind cluster if needed and builds images
2. **Start Platform**: `sh start.sh` - Deploys Darwin platform via Helm

## Access Information

**Datastores Available:**
- MySQL: `darwin-mysql:3306` (root/password)
- Cassandra: `darwin-cassandra:9042` (darwin_user/darwin_password)  
- Kafka: `darwin-kafka:9092` (single broker)
- ZooKeeper: `darwin-zookeeper:2181` (for Helix)
- LocalStack S3: `darwin-localstack:4566`
- Airflow: `airflow.localhost` (darwin/darwin)

**Service Endpoints:**
- Feature Store: `localhost/feature-store/*`
- Feature Store Admin: `localhost/feature-store-admin/*`
- Feature Store Consumer: `localhost/feature-store-consumer/*`
- Workspace: `localhost/workspace/*`
- Airflow UI: `airflow.localhost`

## Adding New Services

### 1. Create Service Directory
```bash
mkdir compute
cd compute
# Pull your repos/code here
```

### 2. Register in services.yaml
```yaml
services:
  compute:
    repository: your-org/compute-service
    dockerfile: Dockerfile
    context: compute/
    build_args:
      - ENV=local
```

### 3. Add Deployment Config
Add to `helm/darwin/charts/services/values.yaml`:
```yaml
compute:
  enabled: true
  replicas: 1
  image:
    repository: localhost:5000/compute-service
    tag: latest
  env:
    - name: CUSTOM_VAR
      value: "your-value"
  resources:
    limits:
      cpu: 500m
      memory: 1Gi
```

### 4. Deploy
```bash
sh setup.sh  # Builds new image
sh start.sh  # Deploys
```

### 5. Add Nginx Routing (Optional)
Add to `helm/darwin/charts/services/values.yaml` under `ingress.paths`:
```yaml
ingress:
  paths:
    - path: "/compute(/|$)(.*)"
      service: "compute"
      port: 8080
```

## Platform Components

- **Datastores**: MySQL, Cassandra, Kafka, ZooKeeper, LocalStack, Airflow
- **Services**: Feature Store applications + your custom services
- **Infrastructure**: Kind cluster, Helm charts, Docker registry
- **Networking**: Nginx Ingress with path-based routing