from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_az_controller(compute: Compute):
    try:
        az = compute.get_az()
        return Response.success_response(message="AZs fetched successfully", data=az)
    except Exception as e:
        logger.error(e)
        return Response.internal_server_error_response(message=e.__str__(), data=None)
