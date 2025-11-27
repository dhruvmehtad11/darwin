from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_instance_role_controller(compute: Compute):
    try:
        roles = compute.get_instance_role()
        logger.debug(f"Instance roles: {roles}")
        return Response.success_response(message="Instance role fetched successfully", data=roles)
    except Exception as e:
        return Response.internal_server_error_response(message=e.__str__(), data=None)
