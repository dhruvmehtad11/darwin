import subprocess
import sys

user_id, project_name, codespace_name, cloned_from, S3_BUCKET = (
    sys.argv[1],
    sys.argv[2],
    sys.argv[3],
    sys.argv[4],
    sys.argv[5],
)

if cloned_from == "None":
    cloned_from = S3_BUCKET + "{}/{}/{}".format(user_id, project_name, codespace_name)

try:
    subprocess.run(["chmod", "+x", "./workspace_core/actors/createWorkspace.sh"])
    subprocess.run(["./workspace_core/actors/createWorkspace.sh", user_id, project_name, codespace_name, cloned_from])
except Exception as e:
    print(e)
