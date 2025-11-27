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
ENV = os.getenv("ENV", "local")

if ENV == "prod":
    log_file_root = os.getenv("LOG_FILE_ROOT_PROD", "/var/www/artifact-builder/logs")
else:
    log_value = os.getenv("LOG_FILE_ROOT_LOCAL", "logs")
    log_file_root = log_value if os.path.isabs(log_value) else os.path.join(REPO_ROOT, log_value)
