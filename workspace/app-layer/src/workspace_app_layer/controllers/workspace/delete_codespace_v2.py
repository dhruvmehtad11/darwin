from workspace_app_layer.utils.utils import error_handler, success_response
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def delete_codespace_v2_controller(workspace, project_id, codespace_id, user: str):
    try:
        workspace.delete_codespace_v2(int(codespace_id))
        response_data = {"project_id": project_id, "codespace_id": codespace_id}
        return success_response(response_data)
    except Exception as err:
        logger.error(f"Error in deleting codespace {codespace_id}: {err}")
        return error_handler(err.__str__())
