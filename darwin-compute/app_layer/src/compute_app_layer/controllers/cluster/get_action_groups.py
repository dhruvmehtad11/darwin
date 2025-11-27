from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_action_groups_controller(compute: Compute, cluster_id: str, offset: int, page_size: int, sort_order: str):
    try:
        action_groups = compute.list_action_groups(cluster_id, offset, page_size, sort_order)
        return {"status": "SUCCESS", "data": action_groups}
    except Exception as e:
        logger.error(e)
        return Response.internal_server_error_response(message=e.__str__(), data=None)
