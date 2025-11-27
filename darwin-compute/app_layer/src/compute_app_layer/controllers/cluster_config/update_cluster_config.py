from loguru import logger

from compute_app_layer.models.cluster_config import ClusterConfigUpdateRequest
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def update_cluster_config_controller(key: str, compute: Compute, request: ClusterConfigUpdateRequest):
    try:
        logger.info(f"Updating cluster config for key: {key} - {request}")
        result = compute.dao.update_cluster_config(key=key, value=request.value)
        logger.info(f"Update cluster config result: {result}")
        return Response.created_response("Cluster config updated successfully")
    except Exception as e:
        logger.exception(f"Error in updating cluster config key {key}: {e}")
        return Response.internal_server_error_response(
            f"Error in updating cluster config key {key}: {e}", {"error": str(e)}
        )
