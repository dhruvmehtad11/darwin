from loguru import logger

from compute_app_layer.models.custom_runtime import CustomRuntimeRequest
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def add_custom_runtime_controller(compute: Compute, request: CustomRuntimeRequest):
    try:
        result = compute.dao.insert_custom_runtime(
            runtime=request.runtime,
            image=request.image,
            namespace=request.namespace,
            created_by=request.created_by,
            type=request.type,
        )
        return Response.success_response(message="Custom Runtime inserted successfully.", data=result)
    except Exception as e:
        logger.exception(f"Error in adding custom runtime, Error: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
