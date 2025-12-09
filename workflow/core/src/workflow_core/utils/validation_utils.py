import os
import re
import asyncio

import requests

from workflow_core.constants.configs import Config
from workflow_core.constants.constants import JOB_CLUSTER, BASIC_CLUSTER, WORKSPACE, FSX_BASE_PATH, \
    FSX_BASE_PATH_DYNAMIC_TRUE, GIT
from workflow_core.error.errors import InvalidWorkflowException
from workflow_core.utils.cluster_utils import ClusterUtils
from workflow_core.utils.logging_util import LoggingUtil
from workflow_model.utils.validators import validate_timezone, validate_and_convert_start_or_end_date, \
    is_valid_timetable
from workflow_model.workflow import CreateWorkflowRequest

LOGGER = LoggingUtil().get_logger()


def _is_valid_name(name: str) -> bool:
    """
    Check if the workflow name is valid.
    """
    if not re.match(r'^[\w.-]+$', name):
        return False
    return True


def is_valid_src_code_path(src_code_path: str) -> bool:
    """
    Validate if the source code path is a valid directory.
    """
    if not os.path.isdir(src_code_path):
        return False
    return True


def is_valid_git_source(task_id):
    return task_id.source.startswith('https://github.com/')


def is_valid_cluster_type(cluster_type):
    print(cluster_type)
    return cluster_type in {JOB_CLUSTER, BASIC_CLUSTER}


async def is_valid_job_cluster_definition(job_cluster_definition_id: str):
    env = os.getenv("ENV", "prod")
    _config = Config(env)
    try:
        # Run blocking HTTP call in thread pool to avoid blocking the event loop
        resp = await asyncio.to_thread(
            requests.get,
            _config.get_app_layer + f"/job-cluster-definitions/{job_cluster_definition_id}",
            timeout=10
        )
        if not 200 <= resp.status_code < 300:
            LOGGER.warning(f"Job cluster validation failed for {job_cluster_definition_id}: status={resp.status_code}")
            return False
        return True
    except (requests.exceptions.ConnectionError, requests.exceptions.Timeout, requests.exceptions.RequestException) as e:
        # If we can't connect to the service (DNS resolution failure, network error, etc.),
        # we can't validate the cluster definition, so return False
        LOGGER.warning(f"Failed to validate job cluster {job_cluster_definition_id}: {e}")
        return False


async def is_valid_cluster_id(cluster_type, cluster_id, compute_utils):
    if cluster_type == JOB_CLUSTER:
        return await is_valid_job_cluster_definition(cluster_id)

    elif cluster_type == BASIC_CLUSTER:
        try:
            # Run blocking HTTP call in thread pool using asyncio.to_thread (Python 3.9+)
            compute_details = await asyncio.to_thread(compute_utils.get_cluster_details, cluster_id)
            LOGGER.info(f"Cluster validation for {cluster_id}: details={compute_details}, status={compute_details.get('status')}")
            result = compute_details.get('status') == 'SUCCESS'
            LOGGER.info(f"Cluster validation result for {cluster_id}: {result}")
            return result
        except (IOError, requests.exceptions.ConnectionError, requests.exceptions.Timeout, requests.exceptions.RequestException) as e:
            # If we can't connect to the service or get an error response,
            # we can't validate the cluster, so return False
            LOGGER.warning(f"Failed to validate cluster {cluster_id}: {e}")
            return False

    return False


def is_valid_entry_point(entry_point: str, source_type: str) -> bool:
    """
    Validate if the entry_point is a valid .py or .ipynb file and if the file exists.
    """
    if source_type == WORKSPACE:
        if not os.path.isfile(entry_point):
            return False

    _, file_extension = os.path.splitext(entry_point)
    return file_extension in ('.py', '.ipynb')


async def validate_workflow(request: CreateWorkflowRequest, wf_dict=None):
    if not _is_valid_name(request.display_name):
        raise InvalidWorkflowException(
            "Invalid workflow name. Only alphanumeric characters, dashes, dots and underscores are allowed.")

    if not is_valid_timetable(request.schedule):
        raise InvalidWorkflowException(
            "Not a valid cron expression for the timetable. Please check https://crontab.guru/ for valid expressions")

    env = 'stag' if os.getenv("ENV", "stag") in ['dev', 'stag'] else os.getenv("ENV", "prod")
    tasks = []
    all_tasks = []
    for task in request.tasks:
        all_tasks.append(task.task_name)
    
    # Create a single ClusterUtils instance to reuse
    cluster_utils = ClusterUtils(env)
    
    # Validate all tasks in parallel for better performance
    async def validate_task(task):
        if not _is_valid_name(task.task_name):
            raise InvalidWorkflowException(
                f"Invalid task name '{task.task_name}'. alphanumeric characters, dashes, dots and underscores are allowed.")

        if not is_valid_cluster_type(task.cluster_type):
            raise InvalidWorkflowException(f"{task.cluster_type} is not a valid cluster type.")

        if not await is_valid_cluster_id(task.cluster_type, task.cluster_id, cluster_utils):
            raise InvalidWorkflowException(f"{task.cluster_id} is not a valid cluster id.")

        if task.task_name in tasks:
            raise InvalidWorkflowException(f"Task '{task.task_name}' is already added to the DAG.")

        if task.source_type == GIT:
            if not is_valid_git_source(task):
                raise InvalidWorkflowException(f"Invalid source for task '{task.task_name}'.")

            if not is_valid_entry_point(task.file_path, task.source_type):
                raise InvalidWorkflowException(f"Invalid entry point for task '{task.file_path}'.")

        if task.source_type == WORKSPACE:
            if not is_valid_src_code_path(FSX_BASE_PATH + task.source):
                raise InvalidWorkflowException(
                    f"Invalid source code path for task '{FSX_BASE_PATH_DYNAMIC_TRUE + task.source}'.")

            file_path = FSX_BASE_PATH + task.source + "/" + task.file_path
            file_path_dynamic_true = FSX_BASE_PATH_DYNAMIC_TRUE + task.source + "/" + task.file_path
            if not is_valid_entry_point(file_path, task.source_type):
                raise InvalidWorkflowException(f"Invalid entry point for task '{file_path_dynamic_true}'.")

        if not all(dep in all_tasks for dep in task.depends_on):
            raise InvalidWorkflowException(f"Invalid dependencies for task '{task.task_name}'")

        tasks.append(task.task_name)
    
    # Validate all tasks concurrently
    LOGGER.info(f"Starting parallel validation of {len(request.tasks)} tasks")
    validation_tasks = [validate_task(task) for task in request.tasks]
    await asyncio.gather(*validation_tasks)
    LOGGER.info(f"Completed parallel validation of {len(request.tasks)} tasks")

    request.timezone = validate_timezone(request.timezone)
    if wf_dict:
        # Since wf_dict is fetched from DB and DB stores in UTC, no need of conversion if not given by user
        if not request.start_date:
            request.start_date = wf_dict.start_date
        else:
            request.start_date = validate_and_convert_start_or_end_date(request.start_date, request.timezone)
        if not request.end_date:
            request.end_date = wf_dict.end_date
        else:
            request.end_date = validate_and_convert_start_or_end_date(request.end_date, request.timezone)
    else:
        request.start_date = validate_and_convert_start_or_end_date(request.start_date, request.timezone)
        request.end_date = validate_and_convert_start_or_end_date(request.end_date, request.timezone)
    return True
