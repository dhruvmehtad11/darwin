from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute


async def get_job_clusters_used_before_days_controller(compute: Compute, offset: int, limit: int, days: int):
    try:
        logger.info(f"Getting all job clusters user before days: {days} from {offset} to {offset+limit}")

        # Getting Job Clusters from ES with given offset and limit
        job_clusters = compute.dao.get_job_cluster_ids(offset=offset, limit=limit)

        # Filtering job clusters by last used before days in SQL
        job_clusters_last_used_before_days = compute.dao.get_clusters_last_used_before_days(
            days=days, cluster_ids=job_clusters
        )

        if not job_clusters or not job_clusters_last_used_before_days:
            logger.error("Job clusters not found")
            return Response.not_found_error_response("Job clusters not found")

        logger.info(f"Job clusters used before {days} days are {job_clusters_last_used_before_days}")

        resp = {"cluster_ids": job_clusters_last_used_before_days}

        return Response.success_response(
            data=resp, message=f"Job clusters used before {days} days fetched successfully"
        )
    except Exception as e:
        logger.exception("Error in getting job clusters")
        return Response.internal_server_error_response(f"Error in getting job clusters: {e}", {"error": str(e)})
