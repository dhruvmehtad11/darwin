from workspace_app_layer.models.workspace.response.launch_codespace_response import CodespacePathDetailsResponse
from workspace_app_layer.utils.utils import get_info_from_codespace_path
from workspace_app_layer.models.workspace.codespace_path_request import CodespacePathRequest
from workspace_app_layer.utils.utils import error_handler
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def get_project_id_and_codespace_id_from_codespace_path(workspace, request: CodespacePathRequest):
    try:
        # example path = '<user_id>/project_name<>/<codespace_name>/<codespace_file_name>'
        codespace_path = request.codespace_path

        logger.info(f"Project Id and Codespace Id for codespace path : {codespace_path}")

        details_from_codespace_path: CodespacePathDetailsResponse = get_info_from_codespace_path(codespace_path)

        resp = workspace.get_project_id_and_codespace_id_from_codespace_path(
            user_id=details_from_codespace_path.user_id,
            project_name=details_from_codespace_path.project_name,
            codespace_name=details_from_codespace_path.codespace_name,
        )

        project_id_and_codespace_id_response = {"status": "SUCCESS", "data": []}

        for item in resp:
            project_id_and_codespace_id_response["data"].append(
                {
                    "project_id": item["project_id"],
                    "codespace_id": item["codespace_id"],
                }
            )

        logger.info(
            f"Project Id and Codespace Id response for codespace path {codespace_path}: {project_id_and_codespace_id_response }"
        )

        return project_id_and_codespace_id_response
    except Exception as err:
        logger.error(
            f"Error in getting Project Id and Codespace Id for codespace path : {codespace_path} with error : {err}"
        )
        return error_handler(err.__str__())
