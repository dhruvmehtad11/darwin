# Darwin Platform Umbrella Chart

This chart has been converted to an umbrella chart architecture where databases are deployed first, followed by application services.

## Architecture Overview

```
darwin (umbrella chart)
├── datastores (subchart) - Deploys first with hook weight -5
│   ├── mysql
│   ├── cassandra
│   ├── zookeeper
│   ├── kafka
│   └── airflow
└── services (subchart) - Deploys after datastores with hook weight 5
    └── feature-store
```

## Deployment Order

1. **Datastores** are deployed first and include:
   - MySQL database
   - Cassandra NoSQL database
   - Zookeeper coordination service
   - Kafka streaming platform
   - Airflow workflow orchestration (optional)

2. **Services** are deployed after databases are ready and include:
   - Feature Store service
   - Feature Store Admin
   - Feature Store Consumer
   - Workflow Orchestration (optional)
   - Compute Orchestration (optional)

## Usage

### Install the complete platform
```bash
helm install darwin . --namespace darwin --create-namespace
```

### Install only datastores
```bash
helm install darwin . --namespace darwin --create-namespace --set services.enabled=false
```

### Install only services (assuming datastores are already running)
```bash
helm install darwin . --namespace darwin --create-namespace --set datastores.enabled=false
```

### Upgrade specific subcharts
```bash
# Update dependencies first
helm dependency update

# Upgrade datastores only
helm upgrade darwin . --reuse-values --set-string 'datastores.image.tag=new-version'

# Upgrade services only  
helm upgrade darwin . --reuse-values --set-string 'services.feature-store.image.tag=new-version'
```

## Configuration

### Global Configuration
Set in the main `values.yaml`:
```yaml
global:
  imageRegistry: docker.io
  storageClass: standard
  namespace: darwin

datastores:
  enabled: true

services:
  enabled: true
```

### Datastore Configuration
Configure in `charts/datastores/values.yaml` or override via main chart:
```yaml
datastores:
  mysql:
    enabled: true
    # MySQL specific configuration
  cassandra:
    enabled: true
    # Cassandra specific configuration
```

### Service Configuration
Configure in `charts/services/values.yaml` or override via main chart:
```yaml
services:
  feature-store:
    enabled: true
    # Feature store specific configuration
```

## Dependencies

The chart uses Helm pre-install hooks and dependency ordering to ensure:
1. Datastores are deployed first (hook weight: -5)
2. Services wait for datastore readiness before deploying (hook weight: 5)
3. A readiness check job verifies all datastores are available before services start

## Development

To modify the chart structure:

1. Update subchart dependencies in respective `Chart.yaml` files
2. Run `helm dependency update` to refresh dependencies
3. Test deployment order with `helm install --dry-run --debug`

## Migration Notes

- Original monolithic templates have been moved to appropriate subcharts
- Values structure has been reorganized to support subchart architecture
- Deployment hooks ensure proper startup ordering
- All existing functionality is preserved while gaining modular deployment capabilities
