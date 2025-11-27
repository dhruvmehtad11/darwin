from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.constant.event_states import ComputeState
from compute_core.dto.chronos_dto import ChronosEvent


async def delete_cluster_controller(compute: Compute, cluster_id: str):
    try:
        res = compute.delete_cluster(cluster_id)
        return {"status": "SUCCESS", "data": {"cluster_id": cluster_id}}
    except Exception as e:
        logger.exception(f"Error in deleting cluster {cluster_id}, Error: {e.__str__()}")
        event = ChronosEvent(
            cluster_id=cluster_id,
            event_type=ComputeState.CLUSTER_DELETION_FAILED.name,
            message=e.__str__(),
        )
        compute.send_event(event)
        return Response.internal_server_error_response(message=e.__str__(), data=None)
