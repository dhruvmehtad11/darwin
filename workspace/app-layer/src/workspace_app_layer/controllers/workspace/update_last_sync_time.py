from workspace_app_layer.models.workspace.update_last_sync_time_request import UpdateLastSyncTimeRequest
from workspace_app_layer.utils.utils import error_handler, success_response
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


async def update_last_sync_time(workspace, request: UpdateLastSyncTimeRequest):
    try:
        update_last_sync_time_resp = workspace.update_last_sync_time(request.codespace_id)
        if update_last_sync_time_resp == 0:
            raise Exception("Failed to update sync location")
        return success_response()
    except Exception as err:
        logger.error(f"Error in update_last_sync_time: {err}")
        return error_handler(err.__str__())
