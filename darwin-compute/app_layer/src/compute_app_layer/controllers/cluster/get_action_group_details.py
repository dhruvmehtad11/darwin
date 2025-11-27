from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_action_group_details_controller(compute: Compute, cluster_runtime_id: str, sort_order: str):
    try:
        action_group_details = compute.get_action_details(cluster_runtime_id, sort_order)
        return {"status": "SUCCESS", "data": action_group_details}
    except Exception as e:
        logger.error(f"Cluster runtime id: {cluster_runtime_id}, Error : {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
