from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_cluster_pods_controller(compute: Compute, cluster_id: str):
    try:
        resp = compute.get_cluster_pods(cluster_id)
        data = {"Resources": resp}
        return Response.success_response(message="Cluster Pods data fetched successfully", data=data)
    except Exception as e:
        logger.error(e)
        return Response.internal_server_error_response(message=str(e), data=e.__str__())
