# Root Cause Analysis and Fix for Silent Errors

## Root Cause Analysis

### 1. **Critical Issue: PVC Stuck in Deletion State**

**Error Message:**
```
0/3 nodes are available: persistentvolumeclaim "darwin-airflow-logs" is being deleted. not found
```

**Root Cause:**
- The PersistentVolumeClaim `darwin-airflow-logs` is stuck in a "being deleted" state
- This prevents Airflow scheduler and webserver pods from scheduling
- Common causes:
  - Pods still referencing the PVC (even if terminating)
  - Finalizers blocking deletion
  - Incomplete deletion process

**Impact:**
- `darwin-airflow-scheduler-8cf8544cd-vcnww` - **Pending** (cannot schedule)
- `darwin-airflow-webserver-54cfc45959-ltrbx` - **Pending** (cannot schedule)

### 2. **Workflow Init Container Waiting**

**Issue:**
- `darwin-workflow-5ddfc5d6cf-9zx5p` stuck in `Init:0/1` state
- Init container `wait-for-datastore-health` waiting for health check job

**Root Cause:**
- The init container logic was not properly handling the case where the job exists but hasn't started yet
- Limited error visibility when health check fails

**Impact:**
- Workflow service cannot start until health check completes
- No clear indication of why health check is delayed

### 3. **MySQL and Localstack Initializing**

**Status:** Running but containers not ready
- This is **normal** during startup - these services take time to initialize
- Not a silent error, just normal initialization

## Fixes Implemented

### Fix 1: PVC Cleanup Hook Job

**File:** `helm/darwin/charts/datastores/templates/airflow-logs-pvc-cleanup.yaml`

**What it does:**
- Runs as a Helm pre-install/pre-upgrade hook (weight: -10, runs first)
- Detects stuck PVCs in deletion state
- Force deletes pods still using the PVC
- Removes finalizers to allow PVC deletion
- Waits for PVC deletion to complete before proceeding

**Key Features:**
- Only runs in local/darwin-local environments
- Automatically cleans up before PVC creation
- Provides detailed logging for debugging

### Fix 2: Enhanced Init Container Error Handling

**File:** `helm/darwin/charts/services/templates/services.yaml`

**Improvements:**
- Better job status detection (checks if job exists first)
- More detailed status reporting (active/succeeded/failed counts)
- Automatic log fetching on health check failure for debugging
- Improved error messages

**Before:**
```bash
⏳ Waiting for health check job to start... (attempt X/60)
```

**After:**
```bash
⏳ Health check still running... (active: 1, succeeded: 0, failed: 0) (attempt X/60)
```

### Fix 3: RBAC Permissions Update

**File:** `helm/darwin/charts/services/templates/services-rbac.yaml`

**Added Permissions:**
- `pods`: get, list, watch, delete (for cleanup job)
- `persistentvolumeclaims`: get, list, watch, patch, delete (for cleanup job)

## How to Apply the Fix

### Immediate Fix (Manual Cleanup)

If you need to fix the current stuck PVC immediately:

```bash
# 1. Force delete pods using the PVC
kubectl delete pod darwin-airflow-scheduler-8cf8544cd-5fktn -n darwin --force --grace-period=0

# 2. Remove finalizers from PVC
kubectl patch pvc darwin-airflow-logs -n darwin -p '{"metadata":{"finalizers":[]}}' --type=merge

# 3. Wait for PVC to be deleted
kubectl wait --for=delete pvc/darwin-airflow-logs -n darwin --timeout=60s

# 4. Redeploy Helm chart
helm upgrade darwin ./helm/darwin -n darwin
```

### Long-term Fix (Automatic)

The fixes are now in place. On the next Helm upgrade/install:

1. The cleanup job will automatically run first (pre-install hook)
2. It will detect and clean up any stuck PVCs
3. The PVC will be recreated cleanly
4. Pods will schedule successfully

## Verification

After applying the fix, verify:

```bash
# Check PVC status
kubectl get pvc -n darwin | grep airflow-logs

# Check pod status
kubectl get pods -n darwin | grep airflow

# Check cleanup job logs (if it ran)
kubectl logs -n darwin -l app.kubernetes.io/component=airflow-logs-cleanup --tail=50
```

## Prevention

The cleanup hook will prevent this issue in the future by:
- Running automatically before PVC creation
- Cleaning up stuck resources proactively
- Providing clear logging for troubleshooting

## Files Changed

1. `helm/darwin/charts/datastores/templates/airflow-logs-pvc-cleanup.yaml` (NEW)
2. `helm/darwin/charts/services/templates/services.yaml` (MODIFIED)
3. `helm/darwin/charts/services/templates/services-rbac.yaml` (MODIFIED)
