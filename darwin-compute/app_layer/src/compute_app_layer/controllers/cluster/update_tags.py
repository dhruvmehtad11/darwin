from typing import List

from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def update_cluster_tags_controller(compute: Compute, cluster_id: str, new_tags: List[str]):
    try:
        updated_resp = compute.update_cluster_tags(cluster_id, new_tags)
        return {"status": "SUCCESS", "data": updated_resp}
    except Exception as e:
        logger.error(f"Error in updating tags for cluster_id {cluster_id}, Error: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
