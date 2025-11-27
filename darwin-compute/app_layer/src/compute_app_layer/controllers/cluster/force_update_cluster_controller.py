from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.constant.event_states import ComputeState
from compute_core.dto.chronos_dto import ChronosEvent
from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.remote_command import RemoteCommand


async def force_update_cluster_controller(compute: Compute, cluster_id: str, remote_command: RemoteCommand):
    try:
        logger.debug(f"Force Update Cluster: {cluster_id}")
        commands = [RemoteCommandDto.from_dict(rc) for rc in remote_command.get_all_of_cluster(cluster_id)]
        compute.force_update_cluster(cluster_id, commands)
        return Response.success_response(
            message="Cluster Force Updated Successfully",
            data={"cluster_id": cluster_id},
        )
    except Exception as e:
        logger.exception(f"Error in force updating cluster: {cluster_id}, error: {e.__str__()}")
        event = ChronosEvent(
            cluster_id=cluster_id,
            event_type=ComputeState.CLUSTER_UPDATION_FAILED.name,
            message=f"Forceful update failed due to an error: {e.__str__()}.",
        )
        compute.send_event(event)
        return Response.internal_server_error_response(message=e.__str__(), data=None)
