from loguru import logger

from compute_app_layer.models.cluster_config import ClusterConfig
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def create_cluster_config_controller(compute: Compute, request: ClusterConfig):
    try:
        logger.info(f"Creating cluster config for key: {request}")
        result = compute.dao.create_cluster_config(key=request.key, value=request.value)
        logger.info(f"Create cluster config result: {result}")
        return Response.created_response("Cluster config created successfully")
    except Exception as e:
        logger.exception(f"Error in creating cluster config {request.key}: {e}")
        return Response.internal_server_error_response(
            f"Error in creating cluster config key {request.key}: {e}", {"error": str(e)}
        )
