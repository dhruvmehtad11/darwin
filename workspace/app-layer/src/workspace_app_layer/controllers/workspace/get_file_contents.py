from workspace_app_layer.models.workspace.get_file_contents_request import GetFileContents

from workspace_app_layer.exceptions.exceptions import BadRequestException
from workspace_app_layer.utils.response_utils import Response
from workspace_core.utils.logging_util import get_logger
from workspace_core.workspaces import WorkspacesSDK

logger = get_logger(__name__)


async def get_file_contents_controller(workspace: WorkspacesSDK, request: GetFileContents):
    try:
        logger.info(f"Read JSON file request received: {request.source_path}")
        resp_data = workspace.get_file_contents(request.source_path)
        return Response.success_response(
            data=resp_data, message=f"File contents retrieved successfully from {request.source_path}"
        )
    except FileNotFoundError as e:
        logger.error(f"File does not exist in EFS {request.source_path}")
        return Response.bad_request_error_response(f"Source path {request.source_path} does not exist in EFS")
    except BadRequestException as e:
        logger.error(f"Bad request: {e.message}")
        return Response.bad_request_error_response(e.message)
    except Exception as e:
        logger.error(f"Failed to retrieve contents of {request.source_path}: {e}")
        return Response.internal_server_error_response(str(e), None)
