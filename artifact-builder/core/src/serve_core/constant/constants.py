import os
from functools import lru_cache
from pathlib import Path
import subprocess
import shutil
import sys

@lru_cache(maxsize=1)
def get_project_root() -> Path:
    """
    Returns the root directory of the project.
    - First tries `git rev-parse` if Git is available.
    - Falls back to searching for pyproject.toml or setup.py.
    - Works on Windows, macOS, and Linux.
    - Cached for performance.
    """
    cur = Path(__file__).resolve()

    # 1️⃣ Try using Git (if available and in PATH)
    git_exe = shutil.which("git")
    if git_exe:
        try:
            root = subprocess.check_output(
                [git_exe, "rev-parse", "--show-toplevel"],
                stderr=subprocess.DEVNULL,
                shell=(sys.platform == "win32"),  # Required for Windows sometimes
            ).decode().strip()
            if root:
                return Path(root)
        except subprocess.SubprocessError:
            pass  # Git repo not found or command failed

    # 2️⃣ Fallback to marker files (Python packaging)
    for parent in cur.parents:
        if (parent / "pyproject.toml").exists() or (parent / "setup.py").exists():
            return parent

    # 3️⃣ Last-resort fallback — 2 levels up
    return cur.parents[1]

REPO_ROOT = get_project_root()

# Use local script (without sudo) for local development, production script for other envs
ENV = os.getenv("ENV", "local")
if ENV == "local":
    SHELL_FILE = "utils/createImageFromGithub_local.sh"
else:
    SHELL_FILE = "utils/createImageFromGithub.sh"

artifacts_value = os.getenv("BUILD_ARTIFACTS_ROOT", "build_artifacts")
ARTIFACTS_ROOT = artifacts_value if os.path.isabs(artifacts_value) else os.path.join(REPO_ROOT, artifacts_value)

CONTAINER_IMAGE_PREFIX = os.getenv("CONTAINER_IMAGE_PREFIX", "darwin")
CONTAINER_IMAGE_PREFIX_GCP = os.getenv("CONTAINER_IMAGE_PREFIX_GCP", "ray-images")
IMAGE_REPOSITORY = os.getenv("IMAGE_REPOSITORY", "serve-app")
AWS_ECR_ACCOUNT_ID = os.getenv("AWS_ECR_ACCOUNT_ID", "")
AWS_ECR_REGION = os.getenv("AWS_ECR_REGION", "us-east-1")
GCP_PROJECT_ID = os.getenv("GCP_PROJECT_ID", "")
GCP_CREDS_PATH = os.getenv("GCP_CREDS_PATH", "/home/admin/gcp_creds.json")
LOCAL_REGISTRY = os.getenv("LOCAL_REGISTRY", "")
