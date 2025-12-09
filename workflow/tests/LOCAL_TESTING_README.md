# Local Testing Guide - Workflow with 10-Minute Cron Schedule

This guide helps you set up and test workflows locally with a cron schedule that runs every 10 minutes.

## Quick Start

### 1. Run the Setup Script

```bash
cd workflow/tests
./setup_local_testing.sh
```

This script will:
- Create FSX workspace directories (`/var/www/fsx/workspace/`)
- Create a test Python script
- Set up the workspace structure for testing
- Display usage instructions

### 2. Start the Workflow Service

```bash
cd workflow/app_layer
export ENV=local
uvicorn workflow_app_layer.main:app --host localhost --port 8000 --reload
```

### 3. Create the Workflow

**Option A: Using the Python test script (Recommended)**
```bash
cd workflow/tests
python3 test_local_workflow.py
```

**Option B: Using curl with V2 API**
```bash
curl -X POST http://localhost:8000/v2/workflow \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"id":5513,"email":"test@dream11.com"}' \
  -d @workflow/tests/local_test_workflow.json
```

**Option C: Using curl with V3 API**
```bash
curl -X POST http://localhost:8000/v3/workflow \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"email":"test@dream11.com"}' \
  -d @workflow/tests/local_test_workflow_v3.json
```

## Cron Schedule Format

The workflow is configured with a cron schedule that runs **every 10 minutes**:

```
*/10 * * * *
```

### Cron Format Explanation

```
┌───────────── minute (0 - 59)
│ ┌───────────── hour (0 - 23)
│ │ ┌───────────── day of month (1 - 31)
│ │ │ ┌───────────── month (1 - 12)
│ │ │ │ ┌───────────── day of week (0 - 6) (Sunday to Saturday)
│ │ │ │ │
* * * * *
```

- `*/10 * * * *` = Every 10 minutes
- `0 */2 * * *` = Every 2 hours
- `0 0 * * *` = Daily at midnight
- `0 0 * * 1` = Every Monday at midnight

### Testing Different Schedules

You can modify the `schedule` field in the JSON files:

```json
{
  "schedule": "*/5 * * * *"   // Every 5 minutes
  "schedule": "*/15 * * * *"  // Every 15 minutes
  "schedule": "0 * * * *"     // Every hour
  "schedule": "0 0 * * *"     // Daily at midnight
}
```

## Workflow JSON Files

### V2 API Workflow (`local_test_workflow.json`)

Uses the legacy V2 API format with:
- `source_type: "git"` - Pulls from GitHub repository
- `dynamic_artifact: true` - Files are fetched dynamically at runtime
- `schedule: "*/10 * * * *"` - Runs every 10 minutes

### V3 API Workflow (`local_test_workflow_v3.json`)

Uses the modern V3 API format with:
- `task_type: "darwin"` - Darwin task type
- `source_type: "Workspace"` - Uses workspace source
- `source: "workspace://test"` - Workspace path
- `dynamic_artifact: true` - Files are fetched dynamically
- `schedule: "*/10 * * * *"` - Runs every 10 minutes

## Test Script Location

The test Python script is created at:
```
/var/www/fsx/workspace/test/test_script.py
```

You can modify this script to test different functionality.

## Checking Workflow Status

### Get Workflow by ID (V2 API)
```bash
curl http://localhost:8000/v2/workflow/{workflow_id} \
  -H 'msd-user: {"id":5513,"email":"test@dream11.com"}'
```

### Get Workflow by ID (V3 API)
```bash
curl http://localhost:8000/v3/workflow/{workflow_id} \
  -H 'msd-user: {"email":"test@dream11.com"}'
```

### List All Workflows (V2 API)
```bash
curl -X POST http://localhost:8000/workflows \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"id":5513,"email":"test@dream11.com"}' \
  -d '{
    "query": "",
    "filters": {"user": [], "status": []},
    "page_size": 100,
    "offset": 0,
    "sort_by": "created_at",
    "sort_order": "desc"
  }'
```

## Troubleshooting

### Issue: "Workflow service is not running"
**Solution:** Start the workflow service:
```bash
cd workflow/app_layer
export ENV=local
uvicorn workflow_app_layer.main:app --host localhost --port 8000 --reload
```

### Issue: "Permission denied" when running setup script
**Solution:** Make the script executable and run with sudo for directory creation:
```bash
chmod +x workflow/tests/setup_local_testing.sh
sudo ./workflow/tests/setup_local_testing.sh
```

### Issue: "Invalid source code path"
**Solution:** Ensure the FSX directories are created:
```bash
sudo mkdir -p /var/www/fsx/workspace/test
sudo chmod -R 777 /var/www/fsx/
```

### Issue: "Workflow name already exists"
**Solution:** Use a unique workflow name or delete the existing workflow first.

## Customizing the Test Workflow

### Change the Schedule

Edit the JSON file and change the `schedule` field:
```json
{
  "schedule": "*/5 * * * *"  // Change to every 5 minutes
}
```

### Use Your Own Python Script

1. Place your script in `/var/www/fsx/workspace/test/`
2. Update the `file_path` in the workflow JSON:
```json
{
  "file_path": "your_script.py"
}
```

### Use Workspace Source Instead of Git

For V2 API, change the task configuration:
```json
{
  "source_type": "Workspace",
  "source": "test",
  "file_path": "test_script.py"
}
```

## Verifying the Cron Schedule

After creating the workflow, you can verify it's scheduled correctly:

1. **Check in Airflow UI** (if running):
   - Navigate to Airflow UI
   - Find your workflow DAG
   - Check the schedule interval

2. **Check via API**:
   ```bash
   curl http://localhost:8000/v2/workflow/{workflow_id} \
     -H 'msd-user: {"id":5513,"email":"test@dream11.com"}' | jq '.data.schedule'
   ```

3. **Check workflow runs**:
   - The workflow should trigger automatically every 10 minutes
   - You can also manually trigger it using the "run_now" endpoint

## Manual Workflow Trigger

To manually trigger a workflow run (without waiting for the schedule):

```bash
# V2 API
curl -X PUT http://localhost:8000/run_now/{workflow_id} \
  -H 'msd-user: {"id":5513,"email":"test@dream11.com"}'

# Or by workflow name
curl -X POST http://localhost:8000/trigger/{workflow_name} \
  -H 'Content-Type: application/json' \
  -H 'msd-user: {"id":5513,"email":"test@dream11.com"}' \
  -d '{"params": {}}'
```

## Next Steps

1. ✅ Run the setup script
2. ✅ Start the workflow service
3. ✅ Create the workflow using one of the methods above
4. ✅ Verify the workflow is created and scheduled
5. ✅ Wait 10 minutes and check if it runs automatically
6. ✅ Check the workflow logs and status

## Additional Resources

- [Cron Expression Guide](https://crontab.guru/)
- [Workflow API Documentation](../README.md)
- [Source and Path Usage Guide](../SOURCE_AND_PATH_USAGE.md)



