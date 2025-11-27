import subprocess
import sys

folder = "~"
for i in sys.argv[1:]:
    folder += f"/{i}"

subprocess.run(f"rm -rf {folder}", shell=True, check=True)
