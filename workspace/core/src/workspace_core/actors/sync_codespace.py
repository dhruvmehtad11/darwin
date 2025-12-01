import json
import subprocess
import sys
import time

import requests
import schedule

user_id, project_name, codespace_name, S3_BUCKET, APP_LAYER_URL, codespace_id = (
    sys.argv[1],
    sys.argv[2],
    sys.argv[3],
    sys.argv[4],
    sys.argv[5],
    sys.argv[6],
)


def sync_codespace():
    resp = subprocess.run(
        ["./workspace_core/actors/syncCodespaceToS3.sh", user_id, project_name, codespace_name, S3_BUCKET],
        stdout=subprocess.PIPE,
    )
    print(resp)
    update_last_sync_time_response = requests.request(
        "PUT", url=update_last_sync_time_url, data=json.dumps(update_last_sync_time_params)
    )
    subprocess.run(f"echo {update_last_sync_time_response}", shell=True)


update_sync_location_url = APP_LAYER_URL + "/update-sync-location"
update_sync_location_params = {
    "codespace_id": codespace_id,
    "sync_location": S3_BUCKET + "{}/{}/{}".format(user_id, project_name, codespace_name),
}
response = requests.request("PUT", url=update_sync_location_url, data=json.dumps(update_sync_location_params))
subprocess.run(f"echo {response}", shell=True)

update_last_sync_time_url = APP_LAYER_URL + "/update-last-sync-time"
update_last_sync_time_params = {"codespace_id": codespace_id}
try:
    subprocess.run(["chmod", "+x", "./workspace_core/actors/syncCodespaceToS3.sh"])
    schedule.every(1).minutes.do(sync_codespace)
    while True:
        schedule.run_pending()
        time.sleep(1)
except Exception as e:
    print(e)
