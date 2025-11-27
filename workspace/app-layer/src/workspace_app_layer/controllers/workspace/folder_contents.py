from workspace_app_layer.models.workspace.folder_contents_request import FolderContentsRequest
from workspace_app_layer.utils.utils import error_handler
from workspace_core.workspaces import WorkspacesSDK
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def folder_contents_controller(workspace: WorkspacesSDK, request: FolderContentsRequest):
    try:
        resp = workspace.folder_contents(folder_path=request.folder_path)
        return {"status": "SUCCESS", "data": resp}
    except Exception as err:
        logger.error(f"Error in folder_contents_controller: {err}")
        return error_handler(err.__str__())
