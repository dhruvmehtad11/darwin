# Source and Path Usage Guide

This document explains how `source` and `file_path` are used for different source types in workflow creation.

## 1. Pulling GitHub Repositories

### Configuration
- **`source_type`**: `"git"` or `"GIT"`
- **`source`**: GitHub repository URL
- **`file_path`**: Path to the Python file within the repository

### Supported GitHub URL Formats

1. **Basic repository URL** (defaults to master/main branch):
   ```json
   {
     "source_type": "git",
     "source": "https://github.com/dream11/darwin-workflow",
     "file_path": "core/tests/test_files/test1.py"
   }
   ```

2. **Repository URL with branch** (using `/tree/`):
   ```json
   {
     "source_type": "git",
     "source": "https://github.com/dream11/darwin-workflow/tree/feat/notification+trigger+abort",
     "file_path": "core/tests/test_files/test1.py"
   }
   ```

3. **Repository URL ending with `.git`**:
   ```json
   {
     "source_type": "git",
     "source": "https://github.com/dream11/darwin-workflow.git",
     "file_path": "core/tests/test_files/test1.py"
   }
   ```

### How It Works

The system processes GitHub URLs in `__build_src_code_git()` function:

```200:238:workflow/core/src/workflow_core/utils/workflow_utils.py
def __build_src_code_git(dag_id: str, repo_url: str, env: str) -> str:
    branch_available = False
    if repo_url.endswith('.git'):
        repo_url = repo_url[:-4]
        parts = repo_url.split('/')
        repo_name = parts[-1]
    elif repo_url.find('tree') != -1:
        parts = repo_url.split('/tree/')
        parts_2 = parts[0].split('/')
        repo_name = parts_2[-1]
        branch = parts[-1]
        branch_available = True
    else:
        parts = repo_url.split('/')
        repo_name = parts[-1]
    
    # Determine the appropriate organization
    org_name = _extract_github_organization(repo_url)
    
    if branch_available:
        ZIP_URL = f"https://api.github.com/repos/{org_name}/{repo_name}/zipball/{branch}"
    else:
        ZIP_URL = f"https://api.github.com/repos/{org_name}/{repo_name}/zipball"
    git_token = _get_git_token_for_repo(repo_url)
    headers = {"Authorization": f"token {git_token}"}
    response = requests.get(ZIP_URL, headers=headers)
    if response.status_code == 200:
        import boto3
        s3 = boto3.client('s3')
        current_datetime = datetime.datetime.now()
        _config = Config(env)
        AIRFLOW_S3_FOLDER = _config.get_airflow_s3_folder
        timestamp_str = current_datetime.strftime("%Y_%m_%d-%H_%M_%S")
        s3_key = f"{AIRFLOW_S3_FOLDER}/zips/{dag_id}-{repo_name}-{timestamp_str}.zip"
        BUCKET_NAME = Config(env).get_s3_bucket
        s3.put_object(Bucket=BUCKET_NAME, Key=s3_key, Body=response.content)
        return "s3://" + BUCKET_NAME + "/" + s3_key
    else:
        print(f"Failed to download the zip file. Status code: {response.status_code}")
```

**Process:**
1. Extracts organization and repository name from URL
2. Detects branch if URL contains `/tree/`
3. Downloads repository as zipball from GitHub API
4. Uploads zip to S3
5. Returns S3 URI for the zip file

**Behavior with `dynamic_artifact`:**
- `dynamic_artifact: true` - Uses `github_repo_to_zip_link()` to get zip URL dynamically at runtime
- `dynamic_artifact: false` - Downloads and uploads to S3 during workflow creation

---

## 2. Using Workspace URLs

### Configuration
- **`source_type`**: `"Workspace"` or `"workspace"`
- **`source`**: Workspace path (can use `workspace://` prefix or just the path)
- **`file_path`**: Path to the Python file within the workspace

### Workspace URL Format

The `workspace://` prefix is optional. The system uses it as-is and constructs full paths using FSX base paths:

```json
{
  "source_type": "Workspace",
  "source": "workspace://test",
  "file_path": "path/to/file.py"
}
```

Or without prefix:
```json
{
  "source_type": "Workspace",
  "source": "user@dream11.com/project/codespace",
  "file_path": "scripts/main.py"
}
```

### How It Works

The system processes workspace paths in `get_job_submit_details()` function:

```466:495:workflow/core/src/workflow_core/utils/workflow_utils.py
def get_job_submit_details(dag_id: str, dynamic_artifact: bool, source_type: str, source: str, file_path: str,
                           pip_packages: dict, env: str):
    entry_point : str = ""
    runtime_env = {}

    if dynamic_artifact:
        # Files will be picked up from source dynamically every time during workflow execution
        if source_type == WORKSPACE:
            source_path = FSX_BASE_PATH_DYNAMIC_TRUE + source
            entry_point = source_path + "/" + file_path
        elif source_type == GIT:
            source_path = github_repo_to_zip_link(source)
            pip_packages['pip'].append('smart_open')
            runtime_env = pip_packages | {"working_dir": source_path}
            entry_point = file_path
    else:
        # Files will be picked up during workflow creation once and uploaded to S3
        if source_type == WORKSPACE:
            source_path = FSX_BASE_PATH + source
            s3_uri_path = __build_src_code(dag_id, source_path, env)
            runtime_env = pip_packages | {"working_dir": s3_uri_path}
            entry_point = file_path
        elif source_type == GIT:
            s3_uri_path = __build_src_code_git(dag_id, source, env)
            runtime_env = pip_packages | {"working_dir": s3_uri_path}
            entry_point = file_path
        elif source_type == ZIP:
            runtime_env = pip_packages | {"working_dir": source}
            entry_point = file_path
    return entry_point, runtime_env
```

**FSX Base Paths:**
- `FSX_BASE_PATH = "/var/www/fsx/workspace/"` - Used when `dynamic_artifact: false`
- `FSX_BASE_PATH_DYNAMIC_TRUE = "/home/ray/fsx/workspace/"` - Used when `dynamic_artifact: true`

**Example:**
- `source: "workspace://test"` + `file_path: "main.py"`
- With `dynamic_artifact: false` → Full path: `/var/www/fsx/workspace/workspace://test/main.py`
- With `dynamic_artifact: true` → Full path: `/home/ray/fsx/workspace/workspace://test/main.py`

**Validation:**
The system validates workspace paths exist:

```207:234:workflow/model/src/workflow_model/utils/validators.py
def validate_source_and_entry_point(source_type: str, source: str, file_path: str) -> None:
    """Validate source and entry point paths.

    Args:
        source_type: Type of source (workspace/git)
        source: Source path or URL
        file_path: Entry point file path

    Raises:
        ValueError: If validation fails
    """
    if source_type == GIT:
        if not source.startswith('https://github.com/'):
            raise ValueError(f"Invalid source for Git: {source}. Must be a GitHub URL.")

    elif source_type == WORKSPACE:
        full_source_path = FSX_BASE_PATH + source
        if not os.path.isdir(full_source_path):
            raise ValueError(f"Invalid source code path: {FSX_BASE_PATH_DYNAMIC_TRUE + source}")

        full_file_path = os.path.join(full_source_path, file_path)
        if not os.path.isfile(full_file_path):
            raise ValueError(f"Invalid entry point: {FSX_BASE_PATH_DYNAMIC_TRUE + source + '/' + file_path}")

    # Validate file extension for both source types
    _, file_extension = os.path.splitext(file_path)
    if file_extension not in ['.py', '.ipynb']:
        raise ValueError(f"Invalid entry point file type: {file_path}. Must be .py or .ipynb")
```

---

## 3. Can We Use Local Files for Testing?

### Current Limitations

**Direct local file paths are NOT supported** in the current implementation. The system expects:
- GitHub URLs for `source_type: "git"`
- Workspace paths (mounted on FSX) for `source_type: "Workspace"`

### Local Development Setup

For local testing, you can set up a mock FSX workspace:

1. **Create local FSX directory structure:**
   ```bash
   sudo mkdir -p /var/www/fsx/workspace
   sudo chmod -R 777 /var/www/fsx/
   ```

2. **Place your test files in the FSX structure:**
   ```bash
   # Example structure
   /var/www/fsx/workspace/test/main.py
   ```

3. **Use workspace source in your workflow:**
   ```json
   {
     "source_type": "Workspace",
     "source": "test",
     "file_path": "main.py"
   }
   ```

### Local Development Script

The workspace service has a local setup script that creates the mock FSX structure:

```1:40:workspace/local/start.sh
#!/bin/bash

#export env
export ENV='local'
#export token
export VAULT_SERVICE_GITHUB_TOKEN='test_token'

#CONFIGS_MAP['local']['fsx_root']=darwin_workspace
fsx_root=darwin_workspace

#grant modification permission for this directory as this is used to mimic EFS
sudo chmod -R 777 /var/www/

#make directory for mocking efs functionalities
sudo mkdir -p /var/www/$fsx_root

#pull images required for ElasticSearch and MySQL remote server connections
docker-compose -f local/docker-compose.yml  pull

#start the ES and Mysql server containers
docker-compose -f local/docker-compose.yml  up -d

#make file executable
chmod +x ./local/setup_for_sql.sh

echo 'setup for sql starts'

# Setup MySQL server
./local/setup_for_sql.sh

#make file executable
chmod +x ./local/setup_for_elasticsearch.sh

echo 'setup for ES starts'

# Setup ES server
./local/setup_for_elasticsearch.sh

# Start the main server using uvicorn
uvicorn app-layer.src.workspace_app_layer.main:app --host localhost --port 8000 --reload
```

### Workaround for Local File Testing

**Option 1: Use Workspace with Local FSX Mount**
- Create files in `/var/www/fsx/workspace/your-test-path/`
- Use `source_type: "Workspace"` with `source: "your-test-path"`

**Option 2: Use GitHub with Local Branch**
- Push your test files to a GitHub branch
- Use `source_type: "git"` with the GitHub URL

**Option 3: Mock the Validation (for unit tests only)**
- In unit tests, you can mock the file system checks
- See test examples in `workflow/tests/v3/`

### Example Test Configuration

From the test fixtures:

```6:32:workflow/app_layer/tests/conftest.py
def workflow_entity():
    return {
        "workflow_name": f"integration_test_workflow_{uuid.uuid4()}",
        "display_name": f"integration_test_display_workflow_{uuid.uuid4()}",
        "description": "integration test workflow",
        "tags": ["test"],
        "schedule": "",
        "retries": 1,
        "notify_on": "",
        "max_concurrent_runs": 1,
        "tasks": [
            {
                "task_name": "logs_poc",
                "source_type": "git",
                "source": "https://github.com/dream11/darwin-workflow",
                "file_path": "core/tests/test_files/test1.py",
                "dynamic_artifact": True,
                "cluster_type": "job",
                "cluster_id": "job-956247bc",
                "dependent_libraries": "",
                "input_parameters": {},
                "retries": 1,
                "timeout": 7200,
                "depends_on": []
            }
        ]
    }
```

---

## Summary

| Source Type | Source Format | File Path | Dynamic Artifact Support |
|------------|---------------|-----------|-------------------------|
| **Git** | `https://github.com/org/repo` or `https://github.com/org/repo/tree/branch` | Relative path from repo root | ✅ Yes |
| **Workspace** | `workspace://path` or just `path` | Relative path from workspace root | ✅ Yes |
| **Local Files** | ❌ Not directly supported | ❌ Must use workspace or git | ❌ N/A |

**Recommendation for Testing:**
- Use workspace source type with files placed in `/var/www/fsx/workspace/` for local testing
- Or use a test GitHub repository/branch for integration testing



