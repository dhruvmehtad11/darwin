from workspace_app_layer.models.workspace.update_sync_location_request import UpdateSyncLocationRequest
from workspace_app_layer.utils.utils import error_handler, success_response
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def update_sync_location(workspace, request: UpdateSyncLocationRequest):
    try:
        update_sync_location_resp = workspace.update_sync_location(request.codespace_id, request.sync_location)
        if update_sync_location_resp == 0:
            raise Exception("Failed to update sync location")
        return success_response()
    except Exception as err:
        logger.error(f"Error in update_sync_location: {err}")
        return error_handler(err.__str__())
