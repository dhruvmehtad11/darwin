# S3 DAG Sync Issue - 0 Files Synced

## Problem
The S3 sync container shows:
```
S3 Sync Successful: 0 files synced
Removed: __pycache__/artefact_local_test_workflow...
```

This means the DAG file hasn't been uploaded to S3 yet.

## Root Cause
When you update a workflow, it triggers a **background task** that:
1. Generates the DAG file
2. Uploads it to S3
3. Airflow sync container then syncs it from S3 to `/opt/airflow/dags/`

The background task may still be running or may have failed.

## Expected S3 Path
- **Bucket**: `darwin-local-bucket`
- **Path**: `darwin_workflow_local/airflow_artifacts/dags/`
- **File**: `artefact_local_test_workflow_10min_v3.py`
- **Full S3 Key**: `s3://darwin-local-bucket/darwin_workflow_local/airflow_artifacts/dags/artefact_local_test_workflow_10min_v3.py`

## Verification Steps

### 1. Check if DAG was uploaded to S3 (LocalStack)
```bash
# Using kubectl exec into localstack pod
kubectl exec -it darwin-localstack-<pod-id> -n darwin -- aws s3 ls s3://darwin-local-bucket/darwin_workflow_local/airflow_artifacts/dags/ --endpoint-url http://localhost:4566
```

### 2. Check Workflow Service Logs
```bash
# Check if background task completed
kubectl logs -n darwin deployment/darwin-workflow --tail=100 | grep -i "dag\|upload\|artefact"
```

### 3. Check Workflow Status
```bash
curl http://localhost/workflow/v3/workflow/wf_id-yqqirik6isc8qc5entrzq7gfi9 \
  -H 'msd-user: {"email":"test@dream11.com"}'
```

Look for `workflow_status` - it should be `active` (not `updating_artifact`)

### 4. Manually Trigger DAG Upload (if needed)
If the background task failed, you can check the workflow service logs or restart the workflow service.

## Expected Behavior

1. **Workflow Update** → Returns SUCCESS immediately
2. **Background Task** → Generates and uploads DAG (takes 10-30 seconds)
3. **S3 Sync** → Syncs DAG from S3 to Airflow (runs every 30 seconds)
4. **Airflow** → Detects new DAG and loads it

## Timeline
- **0-5 seconds**: Workflow update completes
- **5-30 seconds**: Background task uploads DAG to S3
- **30-60 seconds**: S3 sync picks up the DAG file
- **60-90 seconds**: Airflow reloads DAGs and clears "Broken DAG" error

## Troubleshooting

### If DAG still not syncing after 2 minutes:

1. **Check LocalStack S3 bucket**:
   ```bash
   kubectl exec -it <localstack-pod> -n darwin -- \
     aws s3 ls s3://darwin-local-bucket/darwin_workflow_local/airflow_artifacts/dags/ \
     --endpoint-url http://localhost:4566
   ```

2. **Check workflow service logs for errors**:
   ```bash
   kubectl logs -n darwin deployment/darwin-workflow --tail=200 | grep -i error
   ```

3. **Verify S3 sync container is running**:
   ```bash
   kubectl get pods -n darwin | grep s3-dag-sync
   ```

4. **Check sync container logs**:
   ```bash
   kubectl logs -n darwin <airflow-scheduler-pod> -c s3-dag-sync --tail=50
   ```

## Solution

The sync showing "0 files" is **normal** if:
- The background task is still uploading (wait 30-60 seconds)
- The DAG file hasn't been created yet

**Wait 1-2 minutes** and check again. The sync runs every 30 seconds and will pick up the file once it's uploaded.

If after 2 minutes it's still not syncing, check the workflow service logs for upload errors.



