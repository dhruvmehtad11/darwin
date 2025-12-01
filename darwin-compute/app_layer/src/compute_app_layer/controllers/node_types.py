from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def node_types_controller(compute: Compute):
    try:
        data = [{"id": key, "label": value} for key, value in compute.get_node_types().items()]
        return Response.success_response(message="Node Types fetched successfully", data=data)
    except Exception as e:
        logger.error(f"Error in fetching node types: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
