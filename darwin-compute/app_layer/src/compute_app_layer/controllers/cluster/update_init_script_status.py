from loguru import logger

from compute_app_layer.models.update_init_script_status import UpdateInitScriptStatusEntity
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.dto.chronos_dto import ChronosEvent


async def update_init_script_status_controller(compute: Compute, request: UpdateInitScriptStatusEntity, log_path: str):
    try:
        run_id_path = request.uid + ".log"
        action = "Init Script"
        cluster_id = request.cluster_id
        cluster = compute.get_cluster_metadata(request.cluster_id)

        if request.status == "STARTED":
            message = "Started execution" + "##" + log_path + run_id_path
        elif request.status == "FAILED":
            if "worker" in request.message:
                message = request.message
            else:
                message = "Execution Failed" + "##" + log_path + run_id_path
        elif request.status == "SUCCESS":
            message = "Executed Successfully" + "##" + log_path + run_id_path
        else:
            return

        compute.dao.insert_cluster_action(
            run_id=cluster["active_cluster_runid"],
            action=action,
            message=message,
            cluster_id=cluster_id,
            artifact_id=cluster["artifact_id"],
        )

        chronos_event = ChronosEvent(
            cluster_id=cluster_id,
            event_type=f"INIT_SCRIPT_EXECUTION_{request.status}",
            message=message,
            session_id=cluster["active_cluster_runid"],
        )

        compute.send_event(event=chronos_event)

        return {"status": "SUCCESS", "data": None}
    except Exception as e:
        logger.error(f"Error in updating init script status for cluster_id {request.cluster_id}, Error: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
