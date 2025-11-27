from loguru import logger

from compute_app_layer.models.create_cluster import ClusterRequest
from compute_app_layer.utils.response_util import Response
from compute_core.cluster_cost import PredictAPI


async def get_cluster_cost(request: ClusterRequest, predict_api: PredictAPI):
    try:
        head_details = request.head_node_config
        worker_details = request.worker_node_configs
        min_cost, max_cost = predict_api.get_ray_cluster_cost(head_details, worker_details)

        resp = {
            "min_cost": min_cost,
            "max_cost": max_cost,
        }
        return Response.success_response(message="Predicted Min & Max Cost", data=resp)

    except Exception as e:
        logger.exception(f"Cluster cost prediction failed for request: {request} due to error :{e}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
