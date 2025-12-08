# Workflow Module Integration for Darwin

## Overview
This PR adds workflow orchestration capabilities to Darwin using Apache Airflow, enabling users to create, schedule, and monitor data pipelines and ML workflows.

## What's New

### 1. Custom Airflow Operators
Three new custom operators for workflow orchestration:

- **`workflow_operator.py`**: Core workflow operator for Darwin-specific workflow tasks
- **`darwin_operator.py`**: Operator for integrating with Darwin compute clusters and services  
- **`pelican_operator.py`**: Operator for data movement and transformation tasks

These operators are automatically mounted into Airflow via ConfigMap.

### 2. Workflow Service Integration
- New workflow service deployment configuration
- Integration with Darwin's existing services (compute, feature-store, MLflow)
- Proper health checks and init containers for service dependencies

### 3. Airflow Deployment Enhancements
- ConfigMap-based operator injection
- S3 DAG sync from LocalStack
- Improved logging with PVC management
- Better resource allocation and scheduling

### 4. PVC Management
- Automated PVC cleanup for Airflow logs
- Storage class configuration for different environments
- Documented PVC fix procedures (see `PVC_FIX_SUMMARY.md`)

## Key Files Added

```
helm/darwin/charts/datastores/files/operators/
├── darwin_operator.py          # Darwin integration operator
├── pelican_operator.py         # Data pipeline operator  
└── workflow_operator.py        # Core workflow operator

helm/darwin/charts/datastores/templates/
└── airflow-operators-configmap.yaml  # Mounts operators into Airflow

PVC_FIX_SUMMARY.md              # Documentation for PVC issues and fixes
```

## Configuration

### Enable Workflow Module
In `helm/darwin/values.yaml` or your values override:

```yaml
datastores:
  airflow:
    enabled: true
    executor: LocalExecutor  # or CeleryExecutor for production
    
services:
  workflow:
    enabled: true
```

### Workflow Service Environment Variables
The workflow service integrates with:
- **MySQL**: For workflow metadata storage
- **LocalStack/S3**: For DAG storage and artifacts
- **Darwin Compute**: For running compute-intensive tasks
- **Feature Store**: For feature data access
- **MLflow**: For experiment tracking

## Usage Example

```python
from airflow import DAG
from operators.workflow_operator import WorkflowOperator
from operators.darwin_operator import DarwinOperator

with DAG('my_ml_pipeline', schedule_interval='@daily') as dag:
    
    # Create a Darwin compute cluster
    create_cluster = DarwinOperator(
        task_id='create_cluster',
        action='create',
        cluster_config={
            'name': 'training-cluster',
            'workers': 3
        }
    )
    
    # Run training workflow
    train_model = WorkflowOperator(
        task_id='train_model',
        workflow_id='model-training',
        parameters={'epochs': 10}
    )
    
    create_cluster >> train_model
```

## Testing

To test the workflow integration locally:

```bash
# 1. Enable workflow in your setup
./init.sh  # Select workflow when prompted

# 2. Build and deploy
./setup.sh
./start.sh

# 3. Access Airflow UI
kubectl port-forward svc/darwin-airflow-webserver 8080:8080 -n darwin
# Open http://localhost:8080
# Default credentials: admin/admin
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Airflow Scheduler                     │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Custom Operators (via ConfigMap)                  │ │
│  │  - workflow_operator.py                            │ │
│  │  - darwin_operator.py                              │ │
│  │  - pelican_operator.py                             │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────┐
         │     Darwin Workflow Service     │
         │  - Workflow CRUD operations     │
         │  - Run management               │
         │  - Event publishing             │
         └────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌─────────┐    ┌──────────┐    ┌─────────┐
    │ Compute │    │ Feature  │    │ MLflow  │
    │ Clusters│    │  Store   │    │         │
    └─────────┘    └──────────┘    └─────────┘
```

## Migration Notes

### For Existing Darwin Users
- Workflow module is **opt-in** - enable it via `init.sh`
- No breaking changes to existing services
- Workflows can integrate with existing compute clusters and experiments

### Upgrading
If you're upgrading from a version without workflow support:

1. Run `./init.sh` and enable workflow
2. Run `./setup.sh` to build workflow images
3. Run `./start.sh` to deploy

## Known Issues & Limitations

1. **PVC Cleanup**: Airflow logs PVC may need manual cleanup in some scenarios (see `PVC_FIX_SUMMARY.md`)
2. **LocalExecutor**: Default executor is LocalExecutor for simplicity; use CeleryExecutor for production scale
3. **DAG Sync**: Currently syncs from LocalStack S3; configure external S3 for production

## Future Enhancements

- [ ] Add more pre-built operators (Spark, Ray, etc.)
- [ ] Workflow templates and examples
- [ ] Integration with Chronos for event-driven workflows
- [ ] Workflow versioning and rollback
- [ ] Enhanced monitoring and alerting

## Related Documentation

- [PVC Fix Summary](./PVC_FIX_SUMMARY.md) - Troubleshooting PVC issues
- [Darwin Workflow SDK](./workflow/sdk/README.md) - Python SDK for workflows
- [Airflow Documentation](https://airflow.apache.org/docs/)

## Contributors

- @dhruv.mehta - Initial workflow integration

## Questions?

For questions or issues, please:
1. Check the [PVC_FIX_SUMMARY.md](./PVC_FIX_SUMMARY.md) for common issues
2. Review Airflow logs: `kubectl logs -n darwin darwin-airflow-scheduler-xxx`
3. Open an issue in the Darwin repository

