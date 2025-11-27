from typing import List
from loguru import logger

from compute_app_layer.models.recently_visited import RecentlyVisitedRequest
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_model.cluster_status import UIClusterStatusMapping, ClusterStatus
from compute_model.head_node import HeadNode
from compute_model.worker_group import WorkerGroup


def get_total_resources(head_node: HeadNode, worker_groups: List[WorkerGroup]):
    head_node_cores = head_node.node.cores
    head_node_memory = head_node.node.memory

    worker_group_cores = 0
    worker_group_memory = 0
    for w in worker_groups:
        worker_group_cores += w.node.cores * w.max_pods
        worker_group_memory += w.node.memory * w.max_pods

    return {
        "total_cores": head_node_cores + worker_group_cores,
        "total_memory": head_node_memory + worker_group_memory,
    }


async def get_recently_visited_controller(compute: Compute, user: str):
    try:
        resp = compute.get_recently_visited(user)
        result = []

        cluster_ids = []
        for cluster in resp:
            cluster_ids.append(cluster["cluster_id"])

        cluster_status_res = compute.get_clusters_from_list(cluster_ids)

        for cluster in resp:
            cluster_data = compute.get_cluster(cluster["cluster_id"])
            cluster_with_status = [item for item in cluster_status_res if item["cluster_id"] == cluster["cluster_id"]]
            cluster_with_status = cluster_with_status[0]
            total_resources = get_total_resources(cluster_data.head_node, cluster_data.worker_group)
            result.append(
                {
                    "cluster_id": cluster["cluster_id"],
                    "cluster_name": cluster_data.name,
                    "status": UIClusterStatusMapping.get_status(ClusterStatus[cluster_with_status["status"]]),
                    "total_cores": total_resources["total_cores"],
                    "total_memory": total_resources["total_memory"],
                    "last_visited": cluster["visited_at"],
                }
            )
        return Response.success_response("Recently visited data", result)
    except Exception as e:
        logger.exception(f"Error in get_recently_visited_controller for user {user}, Error: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)


async def add_recently_visited_controller(compute: Compute, request: RecentlyVisitedRequest, user: str):
    try:
        resp = compute.add_recently_visited(request.cluster_id, user)
        return Response.success_response("Successfully added to recently visited", {"updatedCount": resp[0]})
    except Exception as e:
        logger.exception(f"Error in get_recently_visited_controller for user {user}, Error: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
