from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def deep_healthcheck_controller(compute: Compute):
    try:
        data = {
            "compute": "SUCCESS" if compute.healthcheck() else "FAILURE",
            "dcm": compute.dcm_healthcheck(),
        }
        return Response.success_response(message="Successful deep healthcheck", data=data)
    except Exception as e:
        logger.exception(f"Error in deep healthcheck: {e}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
