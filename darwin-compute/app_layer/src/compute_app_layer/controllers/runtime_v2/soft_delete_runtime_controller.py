import traceback

from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.runtime_v2 import RuntimeV2


async def soft_delete_runtime_controller(runtimev2: RuntimeV2, runtime: str):
    try:
        logger.info(f"Request: {runtime}")
        result = await runtimev2.soft_delete_runtime(runtime=runtime)
        logger.info(f"Runtime deleted successfully, Result: {result}")
        return Response.success_response(message="Runtime soft deleted successfully", data=result)
    except ValueError as e:
        logger.error(f"Error in soft deleting runtime, Error: {str(e)}")
        return Response.bad_request_error_response(message=str(e), data=None)
    except Exception as e:
        traceback.print_exc()
        logger.exception(f"Error in soft deleting runtime, Error: {str(e)}")
        return Response.internal_server_error_response(message=str(e), data=None)
