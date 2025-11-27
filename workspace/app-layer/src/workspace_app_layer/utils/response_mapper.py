from workspace_app_layer.models.workspace.response.launch_codespace_response import AttachedCluster, ClusterUsage
from workspace_app_layer.utils.utils import get_total_resources, get_active_resources, parse_url
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger
from workspace_core.workspaces import WorkspacesSDK

logger = get_logger(__name__)


def attached_cluster_details(cluster: dict, compute: Compute, workspace: WorkspacesSDK):
    logger.debug(f"Getting attached cluster details for cluster_id: {cluster['cluster_id']}")

    total_resources = get_total_resources(cluster["head_node_config"], cluster["worker_node_configs"])
    logger.debug(f"Total resources for cluster_id {cluster['cluster_id']}: {total_resources}")

    if cluster["status"] == "active":
        used_resources = get_active_resources(compute, cluster["cluster_id"])
        logger.debug(f"Used resources for cluster_id {cluster['cluster_id']}: {used_resources}")

        cluster_usage = ClusterUsage(memory_used=used_resources["memory_used"], cores_used=used_resources["cores_used"])
        ray_dashboard = parse_url(cluster["dashboards"]["data"]["ray_dashboard_url"])
        prometheus_dashboard = parse_url(cluster["dashboards"]["data"]["grafana_dashboard_url"])
        spark_dashboard = parse_url(cluster["dashboards"]["data"]["spark_ui_url"])
    else:
        cluster_usage = ClusterUsage(memory_used=0, cores_used=0)
        ray_dashboard = None
        prometheus_dashboard = None
        spark_dashboard = None

    return AttachedCluster(
        cluster_id=cluster["cluster_id"],
        cluster_name=cluster["name"],
        cluster_status=cluster["status"],
        attached_codespaces_count=workspace.attached_codespaces_count(cluster["cluster_id"]),
        memory=total_resources["total_memory"],
        cores=total_resources["total_cores"],
        cluster_usage=cluster_usage,
        ray_dashboard=ray_dashboard,
        prometheus_dashboard=prometheus_dashboard,
        spark_ui_dashboard=spark_dashboard,
    )
