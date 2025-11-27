from workspace_app_layer.utils.utils import error_handler
from workspace_core.workspaces import WorkspacesSDK
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def get_workspaces_controller(workspace: WorkspacesSDK):
    try:
        resp = workspace.get_workspaces()
        workspaces = []
        for workspace in resp:
            workspaces.append(workspace["user_id"])
        return {"status": "SUCCESS", "data": workspaces}
    except Exception as err:
        logger.error(f"Error in get_workspaces_controller: {err}")
        return error_handler(err.__str__())
