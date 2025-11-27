from workspace_app_layer.utils.utils import error_handler, success_response
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def edit_project_v2_controller(workspace, request):
    try:
        resp = workspace.edit_project_v2(int(request.project_id), request.project_name, request.user)
        return success_response(resp)
    except Exception as err:
        logger.error(f"Error in edit_project_v2_controller {request.project_id}: {err}")
        return error_handler(err.__str__())
