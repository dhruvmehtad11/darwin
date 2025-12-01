from workspace_app_layer.utils.utils import error_handler, success_response
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


async def delete_project_v2_controller(workspace, project_id, user):
    try:
        workspace.delete_project_v2(int(project_id))
        response_data = {"project_id": project_id}
        return success_response(response_data)
    except Exception as err:
        logger.error(f"Error in deleting project {project_id} for user {user} : {err}")
        return error_handler(err.__str__())
