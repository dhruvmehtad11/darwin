import json
from datetime import datetime
from typing import Optional

import requests
from fastapi.responses import JSONResponse

from workspace_app_layer.models.workspace.response.launch_codespace_response import CodespacePathDetailsResponse
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def error_handler(message: str = None):
    if not message:
        message = "Something went wrong"
    return JSONResponse(status_code=500, content={"status": "ERROR", "message": message})


def success_response(data=None):
    response = {"status": "SUCCESS"}
    if data:
        response["data"] = data
    return response


def get_total_resources(head_node, worker_groups):
    head_node_cores = head_node["head_node_cores"]
    head_node_memory = head_node["head_node_memory"]

    worker_group_cores = 0
    worker_group_memory = 0
    for w in worker_groups:
        worker_group_cores += w["cores"] * w["min_pods"]
        worker_group_memory += w["memory"] * w["min_pods"]

    return {"total_cores": head_node_cores + worker_group_cores, "total_memory": head_node_memory + worker_group_memory}


def get_active_resources(compute: Compute, cluster_id: str):
    dashboards = compute.get_cluster_dashboards(cluster_id, internal=True)

    summary_url = f"{dashboards['ray_dashboard_url']}nodes?view=summary"

    try:
        nodes_response = _request(method="GET", url=summary_url, timeout=2, max_retries=3)
        nodes = nodes_response["data"]["summary"]
    except Exception as e:
        return {"cores_used": 0, "memory_used": 0}

    total_cores = 0
    total_cpu_utilization = 0
    memory_used = 0
    memory_total = 0
    nodes_len = len(nodes)
    for node in nodes:
        if "cpu" in node:
            total_cores += node["cpus"][0]
            total_cpu_utilization += (node["cpu"]) * (node["cpus"][0])
            memory_used += node["mem"][3]
            memory_total += node["mem"][0]
        else:
            nodes_len -= 1

    return {
        "cores_used": int(round(total_cpu_utilization / total_cores, 0)) if total_cores else 0,
        "memory_used": int(round(memory_used / memory_total, 2) * 100) if memory_total else 0,
    }


def serialize_date(date: datetime) -> str:
    """
    :param date: datetime
    :return: Date in ISO 8601 format
    """
    date = str(date)
    return date.replace(" ", "T") + "Z"


def parse_url(url: Optional[str]) -> str:
    """
    Removes https from the url.
    :param url: url to remove https from
    :return: url without https
    """
    if not url:
        return url
    return url.replace("https://", "")


def get_info_from_codespace_path(codespace_path: str) -> CodespacePathDetailsResponse:
    codespace_details = codespace_path.split("/")
    user_id, project_name, codespace_name = codespace_details[:3]
    return CodespacePathDetailsResponse(
        user_id=user_id,
        project_name=project_name,
        codespace_name=codespace_name,
    )


def _request(
    method: str,
    url: str,
    headers: Optional[dict] = None,
    data: Optional[dict] = None,
    params: Optional[dict] = None,
    timeout: int = 5,
    max_retries: int = 1,
):
    retries = 0
    while retries < max_retries:
        try:
            response = requests.request(method, url, headers=headers, json=data, timeout=timeout, params=params)
            if not 200 <= response.status_code < 300:
                logger.error(f"Error occurred in API {method} - {url} - {response.text}, body - {data}")
                response.raise_for_status()
            logger.info(f"Successfully fetched response from API {url} with response {response.json()}")
            return response.json()
        except Exception as e:
            logger.error(f"Error occurred in API {method} - {url} - {e}")
            retries += 1
            if retries >= max_retries:
                raise e
