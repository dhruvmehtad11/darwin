from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_model.cluster_status import UIClusterStatusMapping


async def get_filters_controller(compute: Compute):
    try:
        logger.debug("Received request for filter options")
        resp = {"status": [status.name for status in UIClusterStatusMapping], "users": compute.get_all_users()}
        logger.info(f"Response: {resp}")
        return Response.success_response(message="Filter values fetched successfully", data=resp)
    except Exception as e:
        logger.exception(e)
        return Response.internal_server_error_response(message=str(e), data=None)
