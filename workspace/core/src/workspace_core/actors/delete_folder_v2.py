import subprocess
from typing import Optional

from workspace_core.constants.constants import CWD


def delete_folder(fsx_root: str, user_id: str, project_name: str, codespace_name: Optional[str] = None):
    folder = user_id
    folder += "/" + project_name
    if codespace_name:
        folder += "/" + codespace_name

    resp = subprocess.run(["chmod", "+x", "./actors/deleteFolderV2.sh"], capture_output=True, text=True, cwd=CWD)
    if resp.returncode != 0:
        raise Exception("Error in bash script: ", resp.stderr)
    resp = subprocess.run(["./actors/deleteFolderV2.sh", fsx_root, folder], capture_output=True, text=True, cwd=CWD)
    if resp.returncode != 0:
        raise Exception("Error in bash script: ", resp.stderr)
