from workspace_app_layer.utils.utils import get_active_resources, error_handler
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def get_cluster_resources(cluster_id: str, compute: Compute, env):
    try:
        cluster_status = compute.get_cluster_details(cluster_id)
        if cluster_status["status"] == "active":
            resources = get_active_resources(compute, cluster_id)
            return {
                "status": "SUCCESS",
                "data": {"cores_used": resources["cores_used"], "memory_used": resources["memory_used"]},
            }

        return {"status": "ERROR", "message": "Cluster is not active"}
    except Exception as e:
        logger.error(e)
        return error_handler(e.__str__())
