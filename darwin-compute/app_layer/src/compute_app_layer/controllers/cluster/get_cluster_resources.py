from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_model.cluster_status import UIClusterStatusMapping, ClusterStatus


async def get_cluster_resources(cluster_id: str, compute: Compute):
    try:
        logger.info(f"Getting resources for cluster {cluster_id}")
        cluster_status = UIClusterStatusMapping.get_status(
            ClusterStatus[compute.get_cluster_metadata(cluster_id)["status"]].value
        )
        if cluster_status == "active":
            resources = compute.get_active_resources(cluster_id)
            return Response.success_response(message="Resources fetched successfully", data=resources)

        return Response.success_response(message="Cluster is not active", data=None)
    except Exception as e:
        logger.exception(f"Error getting resources for cluster {cluster_id} - {e}")
        return Response.internal_server_error_response(f"Error getting resources for cluster {cluster_id} - {e}", None)
