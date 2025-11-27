import subprocess
from typing import Optional

from workspace_core.constants.constants import CWD


def edit_folder(
    fsx_root: str,
    user_id: str,
    project_name: str,
    codespace_name: Optional[str] = None,
    new_codespace_name: Optional[str] = None,
    new_project_name: Optional[str] = None,
):
    old_folder = user_id
    old_folder += "/" + project_name
    if codespace_name:
        old_folder += "/" + codespace_name

    new_folder = user_id
    if not new_project_name:
        new_folder += "/" + project_name
        new_folder += "/" + new_codespace_name
    else:
        new_folder += "/" + new_project_name

    resp = subprocess.run(["chmod", "+x", "./actors/editFolderV2.sh"], capture_output=True, text=True, cwd=CWD)
    if resp.returncode != 0:
        raise Exception("Error in bash script: ", resp.stderr)
    resp = subprocess.run(
        ["./actors/editFolderV2.sh", fsx_root, old_folder, new_folder], capture_output=True, text=True, cwd=CWD
    )
    if resp.returncode != 0:
        raise Exception("Error in bash script: ", resp.stderr)
