from loguru import logger

from compute_app_layer.models.jupyter_request import JupyterRequest
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.constant.config import Config
from compute_core.jupyter import Jupyter

jupyter = Jupyter()


async def restart_jupyter_controller(compute: Compute, request: JupyterRequest, config: Config):
    try:
        resp = jupyter.restart_jupyter(compute=compute, config=config, params=request)
        return Response.success_response("Restarted successfully", resp)
    except Exception as e:
        logger.error(f"Error in delete_jupyter_controller: {e}")
        resp = {"error": str(e)}
        return Response.internal_server_error_response("Failed to restart_jupyter_controller", resp)
