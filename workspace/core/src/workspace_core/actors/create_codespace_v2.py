import subprocess
from typing import Optional

from workspace_core.constants.constants import CWD


def create_codespace(fsx_root: str, user_id: str, project_name: str, codespace_name: str, cloned_from: Optional[str]):
    if cloned_from:
        cloned_from = fsx_root + "{}/{}/{}".format(user_id, project_name, cloned_from)
    else:
        cloned_from = "None"

    resp = subprocess.run(["chmod", "+x", "./actors/createWorkspaceV2.sh"], capture_output=True, text=True, cwd=CWD)
    if resp.returncode != 0:
        raise Exception("Error in bash script: ", resp.stderr)

    resp = subprocess.run(
        ["./actors/createWorkspaceV2.sh", fsx_root, user_id, project_name, codespace_name, cloned_from],
        capture_output=True,
        text=True,
        cwd=CWD,
    )
    if resp.returncode != 0:
        raise Exception("Error in bash script", resp.stderr)
