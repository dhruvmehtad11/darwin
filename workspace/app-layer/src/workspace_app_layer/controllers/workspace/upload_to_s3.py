from workspace_app_layer.models.workspace.upload_to_s3_request import UploadToS3Request
from workspace_app_layer.exceptions.exceptions import BadRequestException
from workspace_app_layer.utils.response_utils import Response
from workspace_core.utils.logging_util import get_logger
from workspace_core.workspaces import WorkspacesSDK

logger = get_logger(__name__)


async def upload_to_s3_controller(workspace: WorkspacesSDK, request: UploadToS3Request):
    try:
        logger.info(f"Upload to S3 request received: {request}")
        resp = workspace.upload_to_s3(request.source_path, request.s3_bucket, request.destination_path)
        return Response.success_response(f"Uploaded {request.source_path} to S3")
    except FileNotFoundError as e:
        logger.error(f"File does not exist in EFS {request.source_path}")
        return Response.bad_request_error_response(f"Source path {request.source_path} does not exist in EFS")
    except BadRequestException as e:
        logger.error(f"Bad request in uploading to S3 {request}: {e.message}")
        return Response.bad_request_error_response(e.message)
    except Exception as e:
        logger.error(f"Error in uploading to S3 {request}: {e}")
        if "Access Denied" in str(e):
            return Response.forbidden_error_response(f"S3 bucket {request.s3_bucket} is not accessible")
        return Response.internal_server_error_response(str(e), None)
