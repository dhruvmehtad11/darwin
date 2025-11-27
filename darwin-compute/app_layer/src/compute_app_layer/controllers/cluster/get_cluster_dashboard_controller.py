from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_cluster_dashboard_controller(compute: Compute, cluster_id: str, internal: bool = False):
    try:
        if internal:
            resp = compute.get_internal_dashboards(cluster_id=cluster_id)
        else:
            resp = compute.get_dashboards(cluster_id=cluster_id)
        return Response.success_response(message="Cluster Dashboards Fetched Successfully", data=resp)
    except Exception as e:
        logger.exception(e)
        return Response.internal_server_error_response(message="Error in fetching cluster dashboards", data=e.__str__())
