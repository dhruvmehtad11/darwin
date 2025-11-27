from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.constant.config import Config
from compute_core.constant.constants import ResourceType
from compute_core.util.utils import urljoin


async def get_jupyter_path_controller(compute: Compute, config: Config, release_name: str):
    try:
        cloud_env = config.get_cloud_env(
            default_cloud_env=compute.get_resource_default_cloud_env(ResourceType.REMOTE_KERNEL)
        )
        result = urljoin(config.host_url, cloud_env, f"{release_name}-jupyter", https=True)
        resp = {"jupyterLink": result}
        return Response.success_response("Successfully fetched the path", resp)
    except Exception as e:
        logger.error(f"Error in getting jupyter path: {e}")
        return Response.internal_server_error_response("Error in getting jupyter path", {"error": e})
