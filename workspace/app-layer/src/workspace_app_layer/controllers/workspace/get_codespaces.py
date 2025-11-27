from typing import List

from workspace_app_layer.utils.utils import error_handler
from workspace_core.dto.response import CodespaceResponse
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def get_codespaces_by_project(workspace, project_id: int):
    try:
        resp: List[CodespaceResponse] = workspace.list_codespaces(project_id)
        logger.info(f"Codespaces for project {project_id}: {resp}")
    except Exception as err:
        logger.error(f"Error in getting codespaces for project {project_id}")
        return error_handler(err.__str__())

    codespaces_response = {"status": "SUCCESS", "data": []}
    for codespace in resp:
        codespaces_response["data"].append(
            {"codespace_id": codespace.id, "codespace_name": codespace.name, "created_by": codespace.user_id}
        )
    return codespaces_response
