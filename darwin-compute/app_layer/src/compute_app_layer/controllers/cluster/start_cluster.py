from loguru import logger

from compute_app_layer.utils.response_handlers import ResponseHandler
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.dto.library_dto import LibraryStatus
from compute_core.remote_command import RemoteCommand
from compute_core.util.package_management.package_manager import LibraryManager


async def start_cluster_controller(
    compute: Compute, cluster_id: str, user: str, lib_manager: LibraryManager, remote_command: RemoteCommand
):
    with ResponseHandler() as handler:
        logger.debug(f"Starting cluster: {cluster_id} requested by user {user}")
        lib_manager.update_libraries_for_cluster(cluster_id=cluster_id, status=LibraryStatus.RUNNING.value)
        remote_command.update_execution_status_to_running(cluster_id=cluster_id)
        start_cluster_resp = compute.start(cluster_id, user=user)
        handler.response = Response.accepted_response("Cluster Start Accepted", start_cluster_resp)

    return handler.response
