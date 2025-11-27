from loguru import logger

from compute_app_layer.models.request.cluster_search import SearchClusterRequest
from compute_app_layer.utils.response_util import Response
from compute_core.cluster_resource import get_cluster_resources_batch
from compute_core.compute import Compute
from compute_core.dao.async_cluster_dao import search_cluster
from compute_core.dto.cluster_resource_dto import RayClusterResourceStats
from compute_model.cluster_status import UIClusterStatusMapping, ClusterStatus


async def search_v2_controller(request: SearchClusterRequest, compute: Compute):
    """
    Enhanced search controller with resource information
    Returns clusters with pagination and enhanced data including cores/memory usage
    """
    try:
        # Get search results from Elasticsearch
        search_cluster_res, result_size = await search_cluster(
            request.query, request.filters, request.sort_by, request.sort_order, request.page_size, request.offset
        )

        # Get used and total resources for all clusters
        resource_results_dict: dict[str, RayClusterResourceStats] = await get_cluster_resources_batch(
            search_cluster_res, compute
        )

        # Build response data
        response_data = []

        for cluster in search_cluster_res:
            cluster_id = cluster["cluster_id"]

            resource_stats = resource_results_dict[cluster_id]

            # Build cluster data
            cluster_data = {
                "cluster_id": cluster_id,
                "name": cluster["name"],
                "status": UIClusterStatusMapping.get_status(ClusterStatus(cluster["status"])),
                "runtime": cluster["runtime"],
                "tags": cluster.get("tags", []),
                "labels": cluster.get("labels", {}),
                "cpu": resource_stats.cpu,
                "memory": resource_stats.memory,
                "cost": cluster.get("estimated_cost", "N/A"),
                "created_by": cluster["user"],
                "created_on": cluster["created_on"],
                "last_used_on": cluster["last_used"],
            }

            response_data.append(cluster_data)

        # Build final response
        response = {
            "pagination": {"total_matches": result_size, "page_size": request.page_size, "offset": request.offset},
            "body": response_data,
        }

        return Response.success_response(message="Clusters retrieved successfully", data=response)

    except Exception as e:
        logger.error(f"Error in search_v2_controller: {e}")
        return Response.internal_server_error_response(message=f"Error searching clusters: {str(e)}", data=None)
