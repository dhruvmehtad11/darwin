from typing import List

from workspace_app_layer.models.workspace.get_all_cluster_request import GetAllClusterRequest
from workspace_app_layer.utils.utils import error_handler, get_total_resources
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def calculate_cluster_detail(cluster: dict):
    total_resources = get_total_resources(cluster["head_node_config"], cluster["worker_node_configs"])

    return {
        "id": cluster["cluster_id"],
        "name": cluster["name"],
        "cores": total_resources["total_cores"],
        "memory": total_resources["total_memory"],
        "status": cluster["status"],
    }


def get_all_clusters_response(data: List[dict], result_size: int, compute: Compute, request: GetAllClusterRequest):
    clusters = []
    for cluster in data:
        cluster_info = compute.get_cluster_details(cluster["cluster_id"])
        cluster_detail = calculate_cluster_detail(cluster_info)
        clusters.append(cluster_detail)
    return {
        "status": "SUCCESS",
        "pageSize": request.page_size,
        "offset": request.offset,
        "resultSize": result_size,
        "data": clusters,
    }


async def get_all_clusters(compute: Compute, request: GetAllClusterRequest):
    try:
        response = compute.get_all_clusters_metadata(request)
        logger.debug(f"Get all clusters response: {response}")
        return get_all_clusters_response(response.get("data"), response.get("result_size"), compute, request)
    except Exception as e:
        logger.error(f"Error fetching Clusters {request}: {e}")
        return error_handler("Error fetching Clusters")
