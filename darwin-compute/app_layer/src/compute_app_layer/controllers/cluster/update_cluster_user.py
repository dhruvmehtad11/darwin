from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from loguru import logger


async def update_cluster_user_controller(compute: Compute, cluster_id: str, new_user: str, user: str):
    try:
        updated_resp = compute.update_cluster_user(cluster_id, new_user)
        logger.info(f"{cluster_id} user update was made by user: {user}")
        return Response.success_response(message="Cluster user was Updated", data=updated_resp)
    except Exception as e:
        logger.exception(f"Error in updating user for cluster {cluster_id}: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
