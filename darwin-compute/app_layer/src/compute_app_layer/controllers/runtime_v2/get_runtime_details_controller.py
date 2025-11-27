import traceback

from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.runtime_v2 import RuntimeV2


async def get_runtime_details_controller(runtimev2: RuntimeV2, runtime: str):
    try:
        logger.info(f"Request: {runtime}")
        result = await runtimev2.get_runtime_details(runtime=runtime)
        logger.info(f"Runtime details fetched successfully, Result: {result}")
        return Response.success_response(message="Runtime details fetched successfully", data=result)
    except ValueError as e:
        logger.error(f"Error in getting runtime details, Error: {str(e)}")
        return Response.bad_request_error_response(message=str(e), data=None)
    except Exception as e:
        traceback.print_exc()
        logger.exception(f"Error in getting runtime details, Error: {str(e)}")
        return Response.internal_server_error_response(message=str(e), data=None)
