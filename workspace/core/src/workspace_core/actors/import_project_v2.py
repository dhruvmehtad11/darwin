import subprocess

from workspace_core.constants.constants import CWD


def import_project(fsx_root: str, user_id: str, cloned_from: str, project_name: str, codespace_name: str):
    resp = subprocess.run(["chmod", "+x", "./actors/importProjectV2.sh"], capture_output=True, text=True, cwd=CWD)
    if resp.returncode != 0:
        raise Exception("Error in bash script: ", resp.stderr)
    resp = subprocess.run(
        ["./actors/importProjectV2.sh", fsx_root, user_id, cloned_from, project_name, codespace_name],
        capture_output=True,
        text=True,
        cwd=CWD,
    )
    if resp.returncode != 0:
        raise Exception("Error in bash script: ", resp.stderr)
