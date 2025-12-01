from loguru import logger

from compute_app_layer.utils.response_handlers import ResponseHandler
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.util.package_management.package_manager import LibraryManager


async def stop_cluster_controller(compute: Compute, cluster_id: str, user: str, lib_manager: LibraryManager):
    with ResponseHandler() as handler:
        logger.debug(f"Stopping cluster: {cluster_id}")
        res = compute.stop(cluster_id, user=user)
        logger.debug(f"Cluster stop response: {res}")

        # Remove uninstalled libraries from the cluster
        lib_manager.delete_uninstalled_library(cluster_id)

        # Update the status of running libraries to created
        lib_manager.update_running_libraries_to_created(cluster_id)

        handler.response = Response.accepted_response("Cluster stopped successfully", {"cluster_id": cluster_id})

    return handler.response
