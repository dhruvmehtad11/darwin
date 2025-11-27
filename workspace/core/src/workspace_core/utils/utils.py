import os
import subprocess
import time
from datetime import datetime
from typing import TypeVar

import boto3
import httpx
import pytz

from workspace_core.constants.constants import *
from workspace_core.utils.event_states import WorkspaceState


State = TypeVar("State", bound=WorkspaceState)


def ist_time():
    timestamp = datetime.now(pytz.timezone("Asia/Kolkata"))
    return timestamp.replace(tzinfo=None).isoformat(timespec="seconds")


def error_handler(message: str = None):
    if message:
        data = message
    else:
        data = GENERIC_ERROR_MESSAGE
    return {"status": ERROR, "data": data}


def get_ray_head_node_link(dashboard_link: str):
    return dashboard_link.split(":8265")[0]


def get_project_name_from_link(github_link: str):
    temp = github_link.split("/")
    return temp[len(temp) - 1].split(".git")[0]


def is_job_successful(response: dict, head_node: str, is_long_running: bool = False):
    status = "PENDING"
    url = head_node + response["job_id"]

    while status != "SUCCEEDED":
        job = httpx.get(url=url)
        status = job.json()["status"]
        if status == "RUNNING" and is_long_running:
            return True
        if status == "FAILED":
            return False
        time.sleep(2)
    return True


def stop_job(url: str, submission_id: str):
    try:
        stop_url = url + submission_id + "/stop"
        resp = httpx.post(url=stop_url)
        return resp
    except Exception as e:
        return e


class Dotdict(dict):
    """dot.notation access to dictionary attributes"""

    __getattr__ = dict.get
    __setattr__ = dict.__setitem__
    __delattr__ = dict.__delitem__


def delete_folder_from_s3(s3_bucket, folder_path):
    s3 = boto3.client("s3")
    files = s3.list_objects_v2(Bucket=s3_bucket, Prefix=folder_path)
    if files["KeyCount"]:
        files_to_delete = [{"Key": f["Key"]} for f in files["Contents"]]
        return s3.delete_objects(Bucket=s3_bucket, Delete={"Objects": files_to_delete})
    s3.close()
    return None


def s3_path_generator(bucket: str, user: str, project_name: str, codespace_name: str = None):
    if not codespace_name:
        return bucket + user + "/" + project_name
    return bucket + user + "/" + project_name + "/" + codespace_name


def update_sync_path(existing_path: str, new_path: str):
    try:
        subprocess.run(
            f'aws s3 sync {existing_path} {new_path} --exclude ".*/*" --exclude ".*" --delete', shell=True, check=True
        )
        return True
    except Exception as e:
        return False


def get_contents(path: str):
    contents = sorted(os.listdir(f"{BASE_EFS_PATH}/{path}"))
    files = []
    sub_folders = []
    for f in contents:
        if os.path.isdir(f"{BASE_EFS_PATH}/{path}/{f}"):
            sub_folders.append({"name": f})
        else:
            files.append({"name": f})
    return {"files": files, "sub_folders": sub_folders}
