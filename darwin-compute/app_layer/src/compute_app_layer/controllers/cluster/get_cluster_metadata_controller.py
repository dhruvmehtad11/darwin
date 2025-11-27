from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_cluster_metadata_controller(compute: Compute, cluster_id: str):
    try:
        resp = compute.get_cluster_metadata(cluster_id)
        return Response.success_response(message="Cluster Metadata fetched successfully", data=resp)
    except Exception as e:
        logger.exception(e)
        return Response.internal_server_error_response(message=str(e), data=e.__str__())
