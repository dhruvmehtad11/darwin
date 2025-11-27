from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_disk_types_controller(compute: Compute):
    try:
        disk_types = compute.get_disk_types()
        return Response.success_response(message="Disk Types fetched successfully", data=disk_types)
    except Exception as e:
        logger.error(f"Error in get_disk_types_controller: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
