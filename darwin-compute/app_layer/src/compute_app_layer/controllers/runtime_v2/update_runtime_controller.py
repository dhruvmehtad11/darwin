import traceback

from loguru import logger

from compute_app_layer.models.runtime_v2 import RuntimeV2Request
from compute_app_layer.utils.response_util import Response
from compute_core.runtime_v2 import RuntimeV2


async def update_runtime_controller(runtimev2: RuntimeV2, request: RuntimeV2Request):
    try:
        logger.info(f"Request: {request}")
        result = await runtimev2.update_runtime(request=request)
        logger.info(f"Runtime updated successfully, Result: {result}")
        return Response.success_response(message="Runtime updated successfully", data=result)
    except ValueError as e:
        logger.error(f"Error in updating runtime, Error: {str(e)}")
        return Response.bad_request_error_response(message=str(e), data=None)
    except Exception as e:
        traceback.print_exc()
        logger.exception(f"Error in updating runtime, Error: {str(e)}")
        return Response.internal_server_error_response(message=str(e), data=None)
