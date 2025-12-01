from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.constant.event_states import ComputeState
from compute_core.dto.chronos_dto import ChronosEvent
from compute_core.dto.exceptions import ClusterInvalidStateException, ClusterNotFoundError
from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.remote_command import RemoteCommand


def _handle_update_cloud_env_error(compute: Compute, cluster_id: str, error: Exception, context: str) -> None:
    """Helper function to handle common error logging and event creation pattern"""
    logger.exception(f"{context} for {cluster_id}, error: {error.__str__()}")
    event = ChronosEvent(
        cluster_id=cluster_id,
        event_type=ComputeState.CLUSTER_UPDATION_FAILED.name,
        message=f"Update Cloud Environment failed due to error: {error.__str__()}.",
    )
    compute.send_event(event)


async def update_cloud_env_controller(
    compute: Compute, remote_command: RemoteCommand, cluster_id: str, cloud_env: str, user: str
):
    try:
        logger.debug(f"Update Cloud Environment for Cluster: {cluster_id} requested")
        commands = [RemoteCommandDto.from_dict(rc) for rc in remote_command.get_all_of_cluster(cluster_id)]
        compute.update_cluster_cloud_env(cluster_id, user, cloud_env, commands)
        return Response.success_response(
            message="Cluster Cloud Environment Updated Successfully",
            data={"cluster_id": cluster_id, "cloud_env": cloud_env},
        )
    except ClusterNotFoundError as e:
        _handle_update_cloud_env_error(compute, cluster_id, e, "Cluster not found while updating cloud environment")
        return Response.not_found_error_response(message=e.__str__(), data=None)
    except (ClusterInvalidStateException, ValueError) as e:
        _handle_update_cloud_env_error(compute, cluster_id, e, "Error in updating cluster cloud environment")
        return Response.bad_request_error_response(message=e.__str__(), data=None)
    except Exception as e:
        _handle_update_cloud_env_error(compute, cluster_id, e, "Unexpected Error in updating cluster cloud environment")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
