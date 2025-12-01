import asyncio

from loguru import logger

from compute_core.compute import Compute
from compute_core.dto.cluster_resource_dto import RayClusterResourceDTO, RayClusterResourceStats
from compute_core.service.ray_cluster import AsyncRayClusterService
from compute_core.util.utils import calculate_active_resource, calculate_total_resources
from compute_model.cluster_status import UIClusterStatusMapping, ClusterStatus


async def get_single_cluster_resources(cluster_id: str, compute: Compute) -> tuple[str, RayClusterResourceDTO]:
    try:
        ray_dashboard = compute.get_internal_dashboards(cluster_id).get("ray_dashboard_url")
        async_ray_service = AsyncRayClusterService(ray_dashboard)
        nodes = await async_ray_service.get_summary()
        return cluster_id, calculate_active_resource(nodes)
    except Exception as e:
        logger.warning(f"Error getting resources for cluster {cluster_id}: {e}")
        return cluster_id, RayClusterResourceDTO(cores_used=0, memory_used=0)


async def get_active_resources_batch(cluster_ids: list[str], compute: Compute) -> dict[str, RayClusterResourceDTO]:
    """
    Batch function to get active resources for multiple clusters concurrently.
    :arg cluster_ids: list of cluster ids.
    :return dict: Mapping of cluster_id to RayClusterResourceDTO
    """
    # Execute all requests concurrently
    tasks = [get_single_cluster_resources(cluster_id, compute) for cluster_id in cluster_ids]
    results = await asyncio.gather(*tasks, return_exceptions=True)

    # Process results and handle exceptions
    resource_map = {}
    for result in results:
        cluster_id, resource_dto = result
        resource_map[cluster_id] = resource_dto

    return resource_map


async def get_cluster_resources_batch(clusters_details: dict, compute: Compute) -> dict[str, RayClusterResourceStats]:
    """
    Batch function to get resource stats for multiple clusters concurrently.
    args: clusters_details: list of clusters details.
    args: compute: Compute object for getting internal ray dashboard links.
    return: dict: Mapping of cluster_id to RayClusterResourceStats
    """
    try:
        logger.debug(f"Getting resources for clusters {clusters_details}")

        # Filter active clusters
        active_cluster_ids = []
        for cluster in clusters_details:
            cluster_id = cluster["cluster_id"]
            cluster_status = UIClusterStatusMapping.get_status(ClusterStatus[cluster["status"]])
            if cluster_status == "active":
                active_cluster_ids.append(cluster_id)
        logger.debug(f"Found {len(active_cluster_ids)} active clusters")

        # Batch fetch used resources for active clusters only
        used_resources_map = {}
        if active_cluster_ids:
            used_resources_map = await get_active_resources_batch(active_cluster_ids, compute)
        logger.debug(f"Found used resources for active clusters: {used_resources_map}")

        # Build result dict for all clusters
        result = {}
        for cluster in clusters_details:
            cluster_id = cluster["cluster_id"]
            resource_stats = RayClusterResourceStats()

            total_resource_capacity = calculate_total_resources(cluster["head_node"], cluster["worker_group"])
            resource_stats.cpu.total = total_resource_capacity["total_cores"]
            resource_stats.memory.total = total_resource_capacity["total_memory"]

            if cluster_id in used_resources_map:
                resources = used_resources_map[cluster_id]
                resource_stats.cpu.usage_in_per = resources.cores_used
                resource_stats.memory.usage_in_per = resources.memory_used

            result[cluster_id] = resource_stats
        logger.debug(f"Found cluster resources for clusters: {result}")

        return result

    except Exception as e:
        logger.error(f"Error in batch resource fetching: {e}, returning zeros for all clusters")
        # Fallback: return zeros for all clusters
        return {cluster["cluster_id"]: RayClusterResourceStats() for cluster in clusters_details}
