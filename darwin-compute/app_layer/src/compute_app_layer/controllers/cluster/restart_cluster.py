from loguru import logger

from compute_app_layer.utils.response_handlers import ResponseHandler
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.dto.library_dto import LibraryStatus
from compute_core.remote_command import RemoteCommand
from compute_core.util.package_management.package_manager import LibraryManager
from compute_model.cluster_status import ClusterStatus


def restart_cluster(
    compute: Compute, cluster_id: str, user: str, lib_manager: LibraryManager, remote_command: RemoteCommand
):
    # Get libraries with UNINSTALL_PENDING status for the cluster
    cluster_libraries = lib_manager.get_cluster_libraries_with_status(cluster_id, LibraryStatus.UNINSTALL_PENDING.value)
    library_ids = [library.id for library in cluster_libraries]

    # Delete these libraries from the cluster
    if len(library_ids) > 0:
        lib_delete_resp = lib_manager.delete_cluster_library(library_ids, cluster_id, ClusterStatus.inactive.value)
        logger.debug(f"Libraries deleted successfully: {lib_delete_resp}")

    # Update the status of the libraries to RUNNING again for the next run
    lib_manager.update_libraries_for_cluster(cluster_id=cluster_id, status=LibraryStatus.RUNNING.value)
    remote_command.update_execution_status_to_running(cluster_id=cluster_id)

    restart_response = compute.restart(cluster_id, user=user)
    return restart_response


async def restart_cluster_controller(
    compute: Compute, cluster_id: str, user: str, lib_manager: LibraryManager, remote_command: RemoteCommand
):
    with ResponseHandler() as handler:
        restart_response = restart_cluster(compute, cluster_id, user, lib_manager, remote_command)
        handler.response = Response.accepted_response("Restart Cluster Accepted", restart_response)

    return handler.response
