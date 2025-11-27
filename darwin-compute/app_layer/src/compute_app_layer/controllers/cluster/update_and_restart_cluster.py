from loguru import logger

from compute_app_layer.controllers.cluster.restart_cluster import restart_cluster
from compute_app_layer.models.create_cluster import ClusterRequest
from compute_app_layer.utils.object_diff import get_diff
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.remote_command import RemoteCommand
from compute_core.util.package_management.package_manager import LibraryManager


def check_is_restart_vars_changed(changed_keys, restart_vars):
    for key in changed_keys:
        if not ("min_pods" in key or "max_pods" in key):
            # If worker_group has only min_pod or max_pod changed then it shouldn't restart
            if any(var in key for var in restart_vars):
                return True


def is_cluster_restart_required(cluster_diff):
    restart_vars = ["runtime", "advance_config", "worker_group", "head_node"]

    if "values_changed" in cluster_diff.keys():
        if check_is_restart_vars_changed(cluster_diff["values_changed"].keys(), restart_vars):
            return True

    if "iterable_item_added" in cluster_diff.keys():
        if check_is_restart_vars_changed(cluster_diff["iterable_item_added"].keys(), restart_vars):
            return True

    if "iterable_item_removed" in cluster_diff.keys():
        if check_is_restart_vars_changed(cluster_diff["iterable_item_removed"].keys(), restart_vars):
            return True

    return False


async def update_and_restart_cluster_controller(
    cluster_id: str,
    request: ClusterRequest,
    user: str,
    compute: Compute,
    remote_command: RemoteCommand,
    lib_manager: LibraryManager,
):
    try:
        logger.debug(f"Updating cluster: {cluster_id}")
        new_cluster_details = request.convert()
        logger.debug(f"New cluster details: {new_cluster_details}")

        old_cluster_details = compute.get_cluster(cluster_id).to_dict()
        logger.debug(f"Old cluster details: {old_cluster_details}")

        diff = get_diff(old_cluster_details, new_cluster_details.to_dict())

        commands = [RemoteCommandDto.from_dict(rc) for rc in remote_command.get_all_of_cluster(cluster_id)]

        if (
            "values_changed" not in diff.keys()
            and "iterable_item_added" not in diff.keys()
            and "iterable_item_removed" not in diff.keys()
        ):
            logger.debug(f"No significant changes detected for cluster: {cluster_id}. Starting the cluster.")
            start_resp = compute.start(cluster_id, user=user)
            logger.debug(f"Cluster start response: {start_resp}")
            return {"status": "SUCCESS", "data": start_resp}

        if is_cluster_restart_required(diff):
            logger.debug(f"Restart required for cluster: {cluster_id}. Updating and restarting the cluster.")
            resp = compute.update_cluster(cluster_id, new_cluster_details, user=user, diff=diff, commands=commands)
            logger.debug(f"Cluster update response: {resp}")
            restart_resp = restart_cluster(
                cluster_id=cluster_id,
                user=user,
                compute=compute,
                remote_command=remote_command,
                lib_manager=lib_manager,
            )
            logger.debug(f"Cluster restart response: {restart_resp}")
            return {"status": "SUCCESS", "data": restart_resp}

        logger.debug(f"Updating and applying changes for cluster: {cluster_id}")
        resp = compute.update_and_apply_changes(
            cluster_id, new_cluster_details, user=user, diff=diff, commands=commands
        )
        logger.debug(f"Cluster update and apply changes response: {resp}")
        return {"status": "SUCCESS", "data": resp}
    except Exception as e:
        import traceback

        logger.error(
            f"Error in updating cluster: {cluster_id}, Error: {e.__str__()} with trace {traceback.format_exc()}"
        )
        logger.exception(f"Error in updating cluster: {cluster_id}, Error: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
