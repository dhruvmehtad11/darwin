from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_runtimes_controller(compute: Compute):
    try:
        runtimes_resp = compute.get_runtimes()
        result = {}

        for runtime in runtimes_resp:
            if not result.get(runtime["type"]):
                result[runtime["type"]] = []
            result[runtime["type"]].append(
                {"name": runtime["runtime"], "created_by": runtime["created_by"], "image": runtime["image"]}
            )
        logger.debug(f"Runtimes fetched successfully: {result}")
        return Response.success_response(message="Runtimes fetched successfully", data=result)
    except Exception as e:
        logger.exception(f"Error while fetching runtimes: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
