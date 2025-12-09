# Workflow Create API Test Results

## Test Summary

Date: $(date)

### JSON Validation Tests

✅ **V2 API Workflow JSON** (`local_test_workflow.json`)
- Status: ✅ Valid JSON
- Workflow Name: `local_test_workflow_10min`
- Schedule: `*/10 * * * *` (runs every 10 minutes)
- Tasks: 1 task
- Task Type: Git source with dynamic artifact

✅ **V3 API Workflow JSON** (`local_test_workflow_v3.json`)
- Status: ✅ Valid JSON
- Workflow Name: `local_test_workflow_10min_v3`
- Schedule: `*/10 * * * *` (runs every 10 minutes)
- Tasks: 1 task
- Task Type: Darwin task with Workspace source

### API Endpoint Tests

⚠️ **Service Status**: Not running (expected for initial test)

To test the actual API endpoints:
1. Start the workflow service:
   ```bash
   cd workflow/app_layer
   export ENV=local
   uvicorn workflow_app_layer.main:app --host localhost --port 8000 --reload
   ```

2. Run the test script:
   ```bash
   cd workflow/tests
   ./test_workflow_api.sh
   ```

## Test Files Created

1. **local_test_workflow.json** - V2 API workflow configuration
2. **local_test_workflow_v3.json** - V3 API workflow configuration
3. **test_workflow_api.sh** - Automated test script
4. **test_local_workflow.py** - Python test script
5. **setup_local_testing.sh** - Local environment setup script

## Cron Schedule Verification

The cron schedule `*/10 * * * *` means:
- **Every 10 minutes** throughout the day
- Format: `minute hour day month weekday`
- `*/10` = every 10 minutes
- `*` = every hour, day, month, weekday

## Next Steps

1. ✅ JSON files validated
2. ⏳ Start workflow service
3. ⏳ Test V2 API endpoint
4. ⏳ Test V3 API endpoint
5. ⏳ Verify workflow creation in database
6. ⏳ Verify workflow scheduled in Airflow

## Quick Test Commands

### Test V2 API
```bash
curl -X POST http://localhost:8000/v2/workflow \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"id":5513,"email":"test@dream11.com"}' \
  -d @workflow/tests/local_test_workflow.json
```

### Test V3 API
```bash
curl -X POST http://localhost:8000/v3/workflow \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"email":"test@dream11.com"}' \
  -d @workflow/tests/local_test_workflow_v3.json
```

### Check Health
```bash
curl http://localhost:8000/healthcheck
```



