from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_tags_controller(compute: Compute):
    try:
        tags = compute.get_tags()
        return Response.success_response(message="Tags Fetched Successfully", data=tags)
    except Exception as e:
        logger.error(f"Error in fetching tags: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
