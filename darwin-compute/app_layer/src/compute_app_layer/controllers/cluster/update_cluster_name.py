from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def update_cluster_name_controller(compute: Compute, cluster_id: str, new_name: str):
    try:
        updated_resp = compute.update_cluster_name(cluster_id, new_name)
        return {"status": "SUCCESS", "data": updated_resp}
    except Exception as e:
        logger.exception(f"Error in updating name for cluster {cluster_id}, Error: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
