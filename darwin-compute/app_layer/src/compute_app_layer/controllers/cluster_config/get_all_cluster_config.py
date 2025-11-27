from loguru import logger

from compute_app_layer.models.cluster_config import ClusterConfig
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_all_cluster_config_controller(compute: Compute, offset: int, limit: int):
    try:
        logger.info(f"Getting all cluster configs from {offset} to {offset + limit}")
        result = compute.dao.get_all_cluster_config(offset=offset, limit=limit)
        logger.info(f"All Cluster configs are: {result}")
        if not result:
            logger.error("Cluster configs not found")
            return Response.not_found_error_response("Cluster configs not found")

        paginated_data = [ClusterConfig(key=row["config_key"], value=row["value"]) for row in result]
        return Response.success_response(data=paginated_data, message="Cluster configs fetched successfully")
    except Exception as e:
        logger.exception("Error in getting cluster configs")
        return Response.internal_server_error_response(f"Error in getting cluster configs: {e}", {"error": str(e)})
