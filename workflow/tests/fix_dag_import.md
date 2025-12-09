# Fix DAG Import Error: ModuleNotFoundError: No module named 'compute_sdk'

## Problem
The generated DAG file `artefact_local_test_workflow_10min_v3.py` has the old import:
```python
from compute_sdk.compute import ComputeCluster
```

But it should be:
```python
from darwin_compute.compute import ComputeCluster
```

## Root Cause
The DAG was generated before the template was updated. The template file (`artefact_template_v3.py`) has been fixed, but the DAG file in Airflow still has the old import.

## Solutions

### Option 1: Update the Workflow (Recommended)
Update the workflow to trigger DAG regeneration:

```bash
curl -X PUT http://localhost/workflow/v3/workflow/wf_id-yqqirik6isc8qc5entrzq7gfi9 \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"email":"test@dream11.com"}' \
  -d @workflow/tests/local_test_workflow_v3.json
```

This will regenerate the DAG with the correct import.

### Option 2: Delete and Recreate
Delete the workflow and create it again:

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

### Option 3: Manual Fix (Temporary)
If you have access to the Airflow DAGs directory or S3, you can manually fix the import in the DAG file:

1. Find the DAG file: `/opt/airflow/dags/artefact_local_test_workflow_10min_v3.py`
2. Change line 16 from:
   ```python
   from compute_sdk.compute import ComputeCluster
   ```
   to:
   ```python
   from darwin_compute.compute import ComputeCluster
   ```
3. Restart Airflow scheduler or wait for it to reload

### Option 4: Verify Airflow Dependencies
Ensure `darwin-compute` is installed in the Airflow environment:

```bash
# In Airflow container/pod
pip install darwin-compute==2.0.3
```

## Verification

After fixing, check the DAG in Airflow:
1. Go to Airflow UI
2. Check if the DAG `local_test_workflow_10min_v3` is no longer broken
3. Verify the import in the DAG file

## Prevention

The template file has been fixed. All new workflows will use the correct import. Only existing workflows need to be updated.



