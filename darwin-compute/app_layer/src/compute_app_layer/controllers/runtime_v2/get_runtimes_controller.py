import traceback

from loguru import logger

from compute_app_layer.models.runtime_v2 import GetRuntimesRequest
from compute_app_layer.utils.response_util import Response
from compute_core.runtime_v2 import RuntimeV2


async def get_runtimes_controller(runtimev2: RuntimeV2, request: GetRuntimesRequest, user: str):
    try:
        logger.info(f"Request: {request}")
        runtimes_resp = await runtimev2.get_all_runtimes(request, user)
        logger.info(f"Runtimes fetched successfully, Result: {runtimes_resp}")
        return Response.success_response(message="Runtimes fetched successfully", data=runtimes_resp)
    except Exception as e:
        traceback.print_exc()
        logger.exception(f"Error while fetching runtimes: {str(e)}")
        return Response.internal_server_error_response(message=str(e), data=None)
