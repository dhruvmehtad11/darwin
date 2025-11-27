from loguru import logger

from compute_app_layer.models.response.cluster_config import ClusterConfigResponse
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_cluster_config_controller(compute: Compute, key: str):
    try:
        logger.info(f"Getting cluster config for key: {key}")
        result = compute.dao.get_cluster_config(key=key)
        logger.info(f"Cluster config result: {result}")
        if not result:
            logger.error(f"Cluster config not found for key: {key}")
            return Response.not_found_error_response(f"Cluster config not found for key: {key}")
        return Response.success_response(
            data=ClusterConfigResponse(value=result[0]["value"]), message="Cluster config fetched successfully"
        )
    except Exception as e:
        logger.exception(f"Error in getting cluster config key {key}: {e}")
        return Response.internal_server_error_response(
            f"Error in getting cluster config key {key}: {e}", {"error": str(e)}
        )
