# Monitor DAG Fix Progress

## Current Status
- ✅ Workflow updated successfully (2025-12-05 00:13:47)
- ⏳ Background task regenerating DAG
- ⏳ Waiting for S3 upload and sync

## Monitoring Steps

### 1. Check Workflow Status (should become `active`)
```bash
curl http://localhost/workflow/v3/workflow/wf_id-yqqirik6isc8qc5entrzq7gfi9 \
  -H 'msd-user: {"email":"test@dream11.com"}' | \
  python3 -c "import json, sys; d=json.load(sys.stdin); print('Status:', d.get('data', {}).get('workflow_status'))"
```

Expected progression:
- `updating_artifact` → `active` (success)
- `updating_artifact` → `creation_failed` (failure - check logs)

### 2. Check Workflow Service Logs
```bash
kubectl logs -n darwin deployment/darwin-workflow --tail=100 | \
  grep -i "dag\|upload\|artefact\|error" | tail -20
```

Look for:
- ✅ "starting the upload" - DAG upload started
- ✅ "DAG ... deployment status: True" - Upload successful
- ❌ Any error messages

### 3. Check S3 for DAG File
```bash
# Get localstack pod name
LOCALSTACK_POD=$(kubectl get pods -n darwin -l app=localstack -o jsonpath='{.items[0].metadata.name}')

# List DAG files in S3
kubectl exec -it $LOCALSTACK_POD -n darwin -- \
  aws s3 ls s3://darwin-local-bucket/darwin_workflow_local/airflow_artifacts/dags/ \
  --endpoint-url http://localhost:4566
```

Expected: `artefact_local_test_workflow_10min_v3.py`

### 4. Verify DAG File Content (if in S3)
```bash
kubectl exec -it $LOCALSTACK_POD -n darwin -- \
  aws s3 cp s3://darwin-local-bucket/darwin_workflow_local/airflow_artifacts/dags/artefact_local_test_workflow_10min_v3.py - \
  --endpoint-url http://localhost:4566 | \
  grep -A 2 "from.*compute"
```

Should show: `from darwin_compute.compute import ComputeCluster` ✅

### 5. Check S3 Sync Logs
In k9s, watch the `s3-dag-sync` container logs. You should see:
```
S3 Sync Successful: 1 files synced
  Synced: artefact_local_test_workflow_10min_v3.py
```

### 6. Check Airflow UI
After 1-2 minutes:
- DAG `local_test_workflow_10min_v3` should no longer show as "Broken"
- No import errors in DAG details

## Timeline

- **0-5s**: Update API returns ✅ (Done)
- **5-30s**: Background task uploads DAG (In Progress)
- **30-60s**: S3 sync picks up DAG
- **60-90s**: Airflow reloads and fixes error

## If Still Broken After 2 Minutes

1. **Check workflow service logs for errors**
2. **Verify darwin-compute is installed in Airflow**:
   ```bash
   kubectl exec -it <airflow-scheduler-pod> -n darwin -c scheduler -- \
     pip list | grep darwin-compute
   ```
3. **Manually check the DAG file in Airflow**:
   ```bash
   kubectl exec -it <airflow-scheduler-pod> -n darwin -c scheduler -- \
     cat /opt/airflow/dags/artefact_local_test_workflow_10min_v3.py | \
     grep -A 2 "from.*compute"
   ```

## Quick Status Check Script

```bash
#!/bin/bash
echo "=== DAG Fix Status ==="
echo ""
echo "1. Workflow Status:"
curl -s http://localhost/workflow/v3/workflow/wf_id-yqqirik6isc8qc5entrzq7gfi9 \
  -H 'msd-user: {"email":"test@dream11.com"}' | \
  python3 -c "import json, sys; d=json.load(sys.stdin); print('  Status:', d.get('data', {}).get('workflow_status', 'N/A'))"
echo ""
echo "2. Recent Workflow Service Logs:"
kubectl logs -n darwin deployment/darwin-workflow --tail=5 | grep -i "dag\|upload" || echo "  No recent DAG activity"
```



