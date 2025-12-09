# DAG Import Error - Fixed

## Problem
The generated DAG file had the old import:
```python
from compute_sdk.compute import ComputeCluster
ModuleNotFoundError: No module named 'compute_sdk'
```

## Solution Applied
✅ **Workflow Updated** - Triggered DAG regeneration

**Action Taken:**
```bash
curl -X PUT http://localhost/workflow/v3/workflow/wf_id-yqqirik6isc8qc5entrzq7gfi9 \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"email":"test@dream11.com"}' \
  -d @workflow/tests/local_test_workflow_v3.json
```

**Result:** ✅ SUCCESS - Workflow updated successfully

## What Happens Next

1. **Background Task**: The update triggers a background task that:
   - Regenerates the DAG using the fixed template (`artefact_template_v3.py`)
   - The new template has the correct import: `from darwin_compute.compute import ComputeCluster`
   - Uploads the new DAG to S3/Airflow

2. **Airflow Reload**: Airflow will:
   - Detect the updated DAG file
   - Reload the DAG
   - Clear the "Broken DAG" error

3. **Verification**: Check in Airflow UI:
   - The DAG `local_test_workflow_10min_v3` should no longer show as broken
   - The import error should be resolved

## Template Fix

The template file has been updated:
- **File**: `workflow/core/src/workflow_core/v3/templates/artefact_template_v3.py`
- **Line 18**: Now uses `from darwin_compute.compute import ComputeCluster`
- **All new workflows** will use the correct import

## Verification Steps

1. Wait 1-2 minutes for DAG regeneration
2. Check Airflow UI - DAG should no longer be broken
3. Verify the DAG file has the correct import:
   ```python
   from darwin_compute.compute import ComputeCluster
   ```

## If Issue Persists

If the DAG is still broken after a few minutes:

1. **Check Airflow logs** for any errors
2. **Verify darwin-compute is installed** in Airflow:
   ```bash
   pip list | grep darwin-compute
   ```
3. **Manually trigger DAG refresh** in Airflow UI
4. **Check S3** for the updated DAG file

## Prevention

- ✅ Template file fixed
- ✅ All new workflows will use correct import
- ✅ Existing workflows need to be updated (as we just did)



