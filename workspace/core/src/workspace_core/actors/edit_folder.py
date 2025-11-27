import subprocess
import sys

existing_path = "~"
for i in sys.argv[1:3]:
    existing_path += f"/{i}"

user = sys.argv[1]
old_project_name = sys.argv[2]
old_codespace_name = sys.argv[3]

new_cs_name = f"{sys.argv[4]}" if sys.argv[4] != "None" else None
new_project_name = f"{sys.argv[5]}" if sys.argv[5] != "None" else None

if not new_project_name:
    existing_path += "/" + old_codespace_name
    new_path = "~" + "/" + user + "/" + old_project_name + "/" + new_cs_name
else:
    new_path = "~" + "/" + user + "/" + new_project_name
    subprocess.run(f"mkdir {new_path}", shell=True, check=True)
    existing_path += "/" + old_codespace_name
    new_path += "/" + old_codespace_name

try:
    subprocess.run(f"mv {existing_path} {new_path}", shell=True, check=True)
    subprocess.run(f"rm -rf {existing_path}", shell=True, check=True)
except Exception as e:
    print(e)
