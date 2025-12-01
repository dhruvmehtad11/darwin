from loguru import logger

from compute_app_layer.models.jupyter_request import JupyterPodRequest
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.constant.config import Config
from compute_core.jupyter import Jupyter

jupyter = Jupyter()


async def start_jupyter_controller(compute: Compute, config: Config, request: JupyterPodRequest):
    try:
        resp = jupyter.start_jupyter(compute, config, request.consumer_id)
        return Response.success_response("Started Jupyter Client", resp)
    except Exception as e:
        logger.exception(f"Error in start_jupyter_controller: {e}")
        resp = {"error": f"{str(e)} from get_jupyter_client"}
        return Response.internal_server_error_response("Failed to start jupyter client", resp)
