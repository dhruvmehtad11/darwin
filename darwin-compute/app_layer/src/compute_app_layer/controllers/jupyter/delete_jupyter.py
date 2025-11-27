from loguru import logger

from compute_app_layer.models.jupyter_request import JupyterRequest
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.constant.config import Config


async def delete_jupyter_controller(compute: Compute, request: JupyterRequest, config: Config):
    try:
        result = compute.dcm.delete_jupyter_client(
            release_name=request.release_name,
            namespace=config.jupyter_namespace,
            kube_config=compute.get_kube_cluster,
        )
        return Response.success_response("DELETED jupyter pod", {})
    except Exception as e:
        logger.error(f"Error in delete_jupyter_controller: {e}")
        return Response.internal_server_error_response("Failed to delete jupyter pod", {"error": str(e)})
