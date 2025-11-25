# Datastores Configuration

The Darwin platform supports multiple datastores for different use cases.

---

## Available Datastores

| Datastore | Purpose | Default Port | Default State |
|-----------|---------|--------------|---------------|
| MySQL | Relational data, service metadata | 3306 | Enabled |
| Cassandra | NoSQL, feature store data | 9042 | Disabled |
| Kafka | Event streaming | 9092 | Disabled |
| ZooKeeper | Coordination (for Kafka/Helix) | 2181 | Disabled |
| LocalStack | AWS S3/SQS mock | 4566 | Enabled |
| OpenSearch | Search & analytics | 9200 | Enabled |
| Airflow | Workflow orchestration | 8080 | Disabled |

---

## Enable/Disable Datastores

**File**: `helm/darwin/values.yaml`

```yaml
datastores:
  enabled: true      # Master switch for all datastores
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
  airflow:
    enabled: false
```

---

## MySQL

### Configuration
**File**: `helm/darwin/charts/datastores/values.yaml`

```yaml
mysql:
  enabled: true
  architecture: standalone
  auth:
    rootPassword: "password"
    database: "darwin"
    username: "root"
    password: "password"
  primary:
    persistence:
      enabled: true
      size: 8Gi
    resources:
      limits:
        cpu: 1000m
        memory: 2Gi
      requests:
        cpu: 250m
        memory: 1Gi
```

### Connection Details
| Property | Value |
|----------|-------|
| Host | `darwin-mysql` |
| Port | `3306` |
| Database | `darwin` |
| Username | `root` |
| Password | `password` |

### Connection String
```
mysql://root:password@darwin-mysql:3306/darwin
```

### Access from Pod
```yaml
env:
  - name: DARWIN_MYSQL_HOST
    value: "darwin-mysql"
  - name: DARWIN_MYSQL_USERNAME
    value: "root"
  - name: DARWIN_MYSQL_PASSWORD
    value: "password"
```

### Init Scripts
**File**: `helm/darwin/charts/datastores/templates/mysql-init-configmap.yaml`

Add SQL files to run on startup:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: mysql-init-scripts
data:
  01-create-databases.sql: |
    CREATE DATABASE IF NOT EXISTS darwin_mlflow;
    CREATE DATABASE IF NOT EXISTS darwin_compute;
```

---

## Cassandra

### Configuration
```yaml
cassandra:
  enabled: true
  cluster:
    name: darwin-cassandra
    replicaCount: 1
  dbUser:
    user: "darwin_user"
    password: "darwin_password"
  persistence:
    enabled: true
    size: 2Gi
  resources:
    limits:
      cpu: 500m
      memory: 1.5Gi
```

### Connection Details
| Property | Value |
|----------|-------|
| Host | `darwin-cassandra` |
| Port | `9042` |
| Username | `darwin_user` |
| Password | `darwin_password` |

### Access from Pod
```yaml
env:
  - name: DARWIN_CASSANDRA_HOST
    value: "darwin-cassandra"
  - name: CASSANDRA_USERNAME
    value: "darwin_user"
  - name: CASSANDRA_PASSWORD
    value: "darwin_password"
```

---

## Kafka

### Configuration
```yaml
kafka:
  enabled: true
  image:
    repository: confluentinc/cp-kafka
    tag: "7.4.0"
  replicaCount: 1
  persistence:
    enabled: true
    size: 2Gi
  resources:
    limits:
      cpu: 500m
      memory: 1Gi
```

### Connection Details
| Property | Value |
|----------|-------|
| Bootstrap Servers | `darwin-kafka:9092` |

### Requires ZooKeeper
Kafka requires ZooKeeper. Enable both:
```yaml
datastores:
  kafka:
    enabled: true
  zookeeper:
    enabled: true
```

### Access from Pod
```yaml
env:
  - name: DARWIN_KAFKA_HOST
    value: "darwin-kafka"
  - name: KAFKA_BOOTSTRAP_SERVERS
    value: "darwin-kafka:9092"
```

---

## ZooKeeper

### Configuration
```yaml
zookeeper:
  enabled: true
  replicaCount: 1
  persistence:
    enabled: false
    size: 1Gi
  resources:
    limits:
      cpu: 300m
      memory: 512Mi
```

### Connection Details
| Property | Value |
|----------|-------|
| Host | `darwin-zookeeper` |
| Port | `2181` |

### Access from Pod
```yaml
env:
  - name: DARWIN_ZOOKEEPER_HOST
    value: "darwin-zookeeper"
  - name: ZOOKEEPER_CONNECT
    value: "darwin-zookeeper:2181"
```

---

## LocalStack (S3/SQS Mock)

### Configuration
```yaml
localstack:
  enabled: true
  image:
    repository: localstack/localstack
    tag: "latest"
  services: "s3,sqs"
  persistence:
    enabled: true
    size: 2Gi
  resources:
    limits:
      cpu: 1000m
      memory: 1Gi
```

### Connection Details
| Property | Value |
|----------|-------|
| Endpoint | `http://darwin-localstack:4566` |
| Region | `us-east-1` |
| Access Key | `test` (any value works) |
| Secret Key | `test` (any value works) |

### Access from Pod
```yaml
env:
  - name: AWS_ENDPOINT_OVERRIDE
    value: "http://darwin-localstack:4566"
  - name: AWS_ENDPOINT_URL_S3
    value: "http://darwin-localstack:4566"
  - name: AWS_ACCESS_KEY_ID
    value: "test"
  - name: AWS_SECRET_ACCESS_KEY
    value: "test"
  - name: AWS_DEFAULT_REGION
    value: "us-east-1"
```

### Create S3 Bucket
```bash
# From within the cluster
aws --endpoint-url=http://darwin-localstack:4566 s3 mb s3://my-bucket

# Or via kubectl exec
kubectl exec -n darwin deployment/darwin-localstack -- \
  awslocal s3 mb s3://my-bucket
```

---

## OpenSearch

### Configuration
```yaml
opensearch:
  enabled: true
  clusterName: darwin-opensearch
  replicaCount: 1
  auth:
    username: "admin"
    password: "admin"
  persistence:
    enabled: true
    size: 2Gi
  env:
    DISABLE_SECURITY_PLUGIN: "true"
    discovery.type: "single-node"
```

### Connection Details
| Property | Value |
|----------|-------|
| Host | `darwin-opensearch` |
| Port | `9200` |
| Username | `admin` |
| Password | `admin` |

### Access from Pod
```yaml
env:
  - name: ELASTICSEARCH_HOST
    value: "darwin-opensearch"
  - name: ELASTICSEARCH_PORT
    value: "9200"
  - name: VAULT_SERVICE_ES_USERNAME
    value: "admin"
  - name: VAULT_SERVICE_ES_PASSWORD
    value: "admin"
```

---

## Airflow

### Configuration
```yaml
airflow:
  enabled: true
  executor: CeleryExecutor
  webserver:
    replicas: 1
    defaultUser:
      enabled: true
      username: darwin
      password: darwin
      email: admin@darwin.local
      role: Admin
  workers:
    replicas: 1
  scheduler:
    replicas: 1
  rabbitmq:
    enabled: true
    auth:
      username: airflow
      password: airflow
```

### Access
| Property | Value |
|----------|-------|
| UI URL | `http://airflow.localhost` |
| Username | `darwin` |
| Password | `darwin` |

---

## Global Database Configuration

Credentials shared across all services:

**File**: `helm/darwin/values.yaml`

```yaml
global:
  database:
    mysql:
      username: "root"
      rootPassword: "password"
      database: "darwin"
      userPassword: "password"
    cassandra:
      username: "darwin_user"
      password: "darwin_password"
    elasticsearch:
      username: "admin"
      password: "admin"
```

Services access via `.Values.global.database.*`

---

## Checking Datastore Health

### Via kubectl
```bash
# MySQL
kubectl exec -n darwin deployment/darwin-mysql -- \
  mysql -u root -ppassword -e "SELECT 1"

# Cassandra
kubectl exec -n darwin statefulset/darwin-cassandra -- \
  cqlsh -u darwin_user -p darwin_password -e "DESCRIBE KEYSPACES"

# LocalStack
kubectl exec -n darwin deployment/darwin-localstack -- \
  awslocal s3 ls

# OpenSearch
kubectl exec -n darwin statefulset/darwin-opensearch -- \
  curl -s localhost:9200/_cluster/health
```

### Via Port Forward
```bash
# MySQL
kubectl port-forward -n darwin svc/darwin-mysql 3306:3306
mysql -h 127.0.0.1 -u root -ppassword

# OpenSearch
kubectl port-forward -n darwin svc/darwin-opensearch 9200:9200
curl http://localhost:9200/_cluster/health
```

---

## Persistence

All datastores support persistence via PVCs:

```yaml
mysql:
  primary:
    persistence:
      enabled: true
      size: 8Gi
      storageClass: ""  # Uses default
```

For local Kind clusters, `hostPath` is used via the `global.local-k8s: true` setting.

---

## Related Prompts

- **Helm charts**: `.prompts/05-helm-charts.md`
- **Services**: `.prompts/07-services.md`
- **Troubleshooting**: `.prompts/08-troubleshooting.md`

