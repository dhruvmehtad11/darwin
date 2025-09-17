# Darwin Platform Deployment Order

## How Sequential Deployment Works

The Darwin umbrella chart ensures databases are deployed before services using Helm pre-install hooks:

### 1. Chart Dependencies
```yaml
# Chart.yaml
dependencies:
  - name: datastores      # Deployed first
  - name: services        # Deployed second
```

### 2. Pre-install Hooks
- **Datastores**: Hook weight `-5` (runs first)
- **Services**: Hook weight `5` (runs after datastores)
  - Includes readiness check that waits for all datastores to be available

## Deployment Sequence

1. **Datastores Pre-install Hook** (`-5`)
   - Executes before any datastores deployment
   
2. **Datastores Deployment**
   - MySQL, Cassandra, Zookeeper, Kafka deploy in parallel
   
3. **Services Pre-install Hook** (`5`)
   - Executes before services deployment
   - **Readiness Check Job**: Waits for all datastores to be available
   - Uses `nc -z` to check MySQL:3306, Cassandra:9042, Kafka:9092
   
4. **Services Deployment**
   - Only starts after readiness check job succeeds
   - Guarantees datastores are available

## Testing Deployment Order

```bash
# Watch deployment progress
kubectl get pods -n darwin -w

# Check pre-install hook logs
kubectl logs -n darwin jobs/darwin-services-readiness-check

# Verify deployment order
helm history darwin -n darwin
```

## Troubleshooting

### Services Not Deploying
If services don't deploy after datastores:

1. Check datastore pod status:
   ```bash
   kubectl get pods -n darwin | grep -E "(mysql|cassandra|kafka)"
   ```

2. Check readiness check job logs:
   ```bash
   kubectl logs -n darwin jobs/darwin-services-readiness-check
   ```

3. Manually test connectivity:
   ```bash
   kubectl run test --image=busybox --rm -it -- nc -z darwin-mysql 3306
   ```

### Force Sequential Deployment
To ensure strict ordering, deploy in phases:

```bash
# Phase 1: Deploy only datastores
helm install darwin . --set services.enabled=false -n darwin

# Wait for datastores to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=mysql -n darwin --timeout=300s

# Phase 2: Deploy services
helm upgrade darwin . --set services.enabled=true -n darwin
```
