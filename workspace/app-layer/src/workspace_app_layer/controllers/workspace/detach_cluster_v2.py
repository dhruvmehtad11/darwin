from workspace_app_layer.models.workspace.detach_cluster_request import DetachClusterRequest
from workspace_app_layer.utils.utils import error_handler
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def detach_cluster_v2_controller(workspace, request: DetachClusterRequest):
    try:
        resp = workspace.detach_cluster_v2(request.codespace_id)
        logger.debug(f"Response from detach_cluster_v2 for request {request}: {resp}")
        if resp == 0:
            raise Exception("No Cluster Attached")
        return {"status": "SUCCESS"}
    except Exception as err:
        logger.error(f"Error in detach_cluster_v2_controller for request {request}: {err}")
        return error_handler(err.__str__())
