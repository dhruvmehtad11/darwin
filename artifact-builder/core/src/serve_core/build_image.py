import json
import os
import subprocess
import threading
import traceback
import uuid
from pathlib import Path
import time
import datetime
from typing import Optional

from loguru import logger

from serve_app_layer.constants.constants import log_file_root
from serve_core.constant.serve_constants import Config
from serve_core.dao.runtime_dao import RuntimeDao
from typeguard import typechecked
from serve_core.constant.constants import SHELL_FILE, ARTIFACTS_ROOT
from serve_core.utils.runtime_utils import download_dockerfile_from_s3, convert_github_url

@typechecked
class Runtimes:
    def __init__(self, env: str):
        self._config = Config(env)
        self.runtime_dao = RuntimeDao(env)
        self.processing_lock = threading.Lock()
        self.env = env

    def healthcheck(self):
        return self.runtime_dao.healthcheck()

    def get_logs_file_path(self, task_id: str):
        _config = Config(self.env)
        app_layer_url = _config.get_app_layer_url
        log_file_path = f"{app_layer_url}/static/{task_id}.log"
        return log_file_path

    def build_image(self, image_tag, app_name, logs_url, build_params):
        exception_occurred = None
        try:
            build_dir = os.path.join(ARTIFACTS_ROOT, image_tag)
            local_docker_file_path = os.path.join(build_dir, "Dockerfile")
            file_path = Path(local_docker_file_path).expanduser()
            dir_path = file_path.parent
            if not dir_path.is_dir():
                dir_path.mkdir(parents=True, exist_ok=True)
            
            build_params = json.loads(build_params)
            docker_file_path = build_params["docker_file_path"]
            
            if docker_file_path:
                if not download_dockerfile_from_s3(docker_file_path, local_docker_file_path):
                    raise Exception("Download from S3 failed")

            shell_file_path = os.path.join(os.getcwd()) + "/" + SHELL_FILE
            git_repo = convert_github_url(build_params["git_repo"])
            branch = build_params["branch"]
            app_dir = build_params["app_dir"]

            filename = logs_url.strip().split("/")[-1]
            logs_url = os.path.join(log_file_root, filename)

            executable = [
                "/bin/bash",
                shell_file_path,
                git_repo,
                branch,
                app_name,
                dir_path,
                image_tag,
                app_dir,
                "False",
            ]

            # Pass current environment variables to the subprocess
            # This ensures LOCAL_REGISTRY and other env vars are available to the script
            env_vars = os.environ.copy()

            def run_process(exe, filename, env):
                nonlocal exception_occurred
                try:
                    with open(filename, "w") as f_obj:
                        subprocess.check_call(exe, stdout=f_obj, stderr=f_obj, env=env)
                except subprocess.CalledProcessError:
                    exception_occurred = Exception("Docker Image Build Failed")

            thread = threading.Thread(target=run_process, args=(executable, logs_url, env_vars))
            thread.start()
            thread.join()

            if exception_occurred:
                raise exception_occurred
            else:
                return True
        except Exception as e:
            print("Exception:", e)
            return False

    def create_task(
            self,
            image_tag: str,
            app_name: str,
            git_repo: str,
            app_dir: str,
            docker_file_path: Optional[str] = "",
            branch: Optional[str] = "",
    ):
        unique_id = uuid.uuid4().hex
        task_id = f"id-{unique_id}"
        logs_url = self.get_logs_file_path(task_id)

        build_params = {
            "git_repo": git_repo,
            "branch": branch,
            "app_dir": app_dir,
            "docker_file_path": docker_file_path,
        }

        self.runtime_dao.create_task(
            task_id=task_id,
            image_tag=image_tag,
            app_name=app_name,
            logs_url=logs_url,
            build_params=build_params,
            status="waiting",
        )
        return task_id, logs_url

    def process_tasks(self):
        while True:
            with self.processing_lock:
                task_id = self.runtime_dao.get_waiting_task_and_update_status()

            logger.info(f"Processing task: {task_id}")
            if task_id:
                start_time = datetime.datetime.now()
                image_tag = self.runtime_dao.get_image_tag(task_id=task_id)
                logs_url = self.runtime_dao.get_logs_url(task_id=task_id)
                app_name = self.runtime_dao.get_app_name(task_id=task_id)
                build_params = self.runtime_dao.get_build_params(task_id=task_id)

                try:
                    status = self.build_image(
                        image_tag=image_tag,
                        app_name=app_name,
                        logs_url=logs_url,
                        build_params=build_params,
                    )

                    if status:
                        elapsed_time = datetime.datetime.now() - start_time
                        if elapsed_time > datetime.timedelta(minutes=60):
                            self.runtime_dao.update_status(task_id, "failed")
                            logger.error(f"Build took > 60 mins for task_id: {task_id}, image_tag: {image_tag}, app_name: {app_name}")
                        else:
                            self.runtime_dao.update_status(task_id, "completed")
                        continue
                    else:
                        self.runtime_dao.update_status(task_id, "failed")
                        logger.error(f"Build failed for task_id: {task_id}, image_tag: {image_tag}, app_name: {app_name}")
                except Exception as e:
                    tb = traceback.format_exc()
                    logger.info(f"Build failed: {e} and stack trace: {tb}")
                    self.runtime_dao.update_status(task_id, "failed")

            else:
                time.sleep(5)

    def get_status(self, task_id):
        status = self.runtime_dao.get_task_status(task_id)
        return status

    def get_task_logs_url(self, task_id: str):
        logs_url = self.runtime_dao.get_logs_url(task_id)
        return logs_url

    def get_tasks(self):
        tasks = self.runtime_dao.get_all_tasks()
        return tasks

