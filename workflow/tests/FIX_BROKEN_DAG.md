# Fix Broken DAG - ModuleNotFoundError: compute_sdk

## Problem
The DAG file in Airflow still has the old import:
```python
from compute_sdk.compute import ComputeCluster
ModuleNotFoundError: No module named 'compute_sdk'
```

## Root Cause
The workflow status shows `creating_failed`, which means the background task that uploads the DAG to S3 failed. The new DAG with the correct import was never uploaded.

## Solution

### Option 1: Update Workflow Again (Recommended)
Update the workflow to trigger a new DAG deployment:

```bash
curl -X PUT http://localhost/workflow/v3/workflow/wf_id-yqqirik6isc8qc5entrzq7gfi9 \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"email":"test@dream11.com"}' \
  -d @workflow/tests/local_test_workflow_v3.json
```

This will:
1. Trigger a new background task
2. Generate DAG with correct import: `from darwin_compute.compute import ComputeCluster`
3. Upload to S3
4. Sync to Airflow

### Option 2: Delete and Recreate
If update fails, delete and recreate:

```bash
# Delete
curl -X DELETE http://localhost/workflow/v3/workflow/wf_id-yqqirik6isc8qc5entrzq7gfi9 \
  -H 'msd-user: {"email":"test@dream11.com"}'

# Recreate
curl -X POST http://localhost/workflow/v3/workflow \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"email":"test@dream11.com"}' \
  -d @workflow/tests/local_test_workflow_v3.json
```

### Option 3: Check Background Task Logs
If updates keep failing, check the workflow service logs:

```bash
kubectl logs -n darwin deployment/darwin-workflow --tail=200 | grep -i "error\|failed\|dag"
```

## Verification

After updating, wait 1-2 minutes and check:

1. **Workflow Status** (should be `active`):
   ```bash
   curl http://localhost/workflow/v3/workflow/wf_id-yqqirik6isc8qc5entrzq7gfi9 \
     -H 'msd-user: {"email":"test@dream11.com"}'
   ```

2. **Check S3 for DAG file**:
   ```bash
   kubectl exec -it <localstack-pod> -n darwin -- \
     aws s3 ls s3://darwin-local-bucket/darwin_workflow_local/airflow_artifacts/dags/ \
     --endpoint-url http://localhost:4566
   ```

3. **Check Airflow UI** - DAG should no longer be broken

## Expected Timeline

- **0-5s**: Update API returns
- **5-30s**: Background task generates and uploads DAG
- **30-60s**: S3 sync picks up new DAG
- **60-90s**: Airflow reloads and fixes error

## Template Fix Confirmed

The template file has the correct import:
- **File**: `workflow/core/src/workflow_core/v3/templates/artefact_template_v3.py`
- **Line 18**: `from darwin_compute.compute import ComputeCluster` ✅

The issue is that the DAG deployment failed, so the old DAG is still in use.



