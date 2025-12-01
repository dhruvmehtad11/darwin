import subprocess
import sys

user_id, cloned_from, project_name, codespace_name = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]

try:
    subprocess.run(["chmod", "+x", "./workspace_core/actors/importProject.sh"])
    subprocess.run(["./mlp_workspaces/actors/importProject.sh", user_id, cloned_from, project_name, codespace_name])
except Exception as e:
    print(e)
