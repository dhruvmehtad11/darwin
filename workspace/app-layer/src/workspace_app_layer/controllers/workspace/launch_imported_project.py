from workspace_app_layer.utils.utils import (
    error_handler,
    get_total_resources,
    get_active_resources,
    serialize_date,
    parse_url,
)
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def launch_imported_project(compute, workspace, codespace_id, github_url):
    try:
        launch_codespace_resp = workspace.launch_imported_project_v2(codespace_id, github_url)
        cluster = launch_codespace_resp["cluster"]

        total_resources = get_total_resources(cluster["head_node_config"], cluster["worker_node_configs"])

        attached_codespaces_count = workspace.attached_codespaces_count(cluster["cluster_id"])

        used_resources = get_active_resources(compute, cluster["cluster_id"])

        return {
            "status": "SUCCESS",
            "project_id": launch_codespace_resp["codespace"].project_id,
            "project_name": launch_codespace_resp["project"].name,
            "codespace_id": codespace_id,
            "codespace_name": launch_codespace_resp["codespace"].name,
            "github_link": github_url,
            "last_sync_time": serialize_date(launch_codespace_resp["codespace"].last_synced_at),
            "jupyter_lab_link": parse_url(launch_codespace_resp["jupyter_link"]),
            "attached_cluster": {
                "cluster_id": cluster["cluster_id"],
                "cluster_name": cluster["name"],
                "cluster_status": cluster["status"],
                "memory": total_resources["total_memory"],
                "cores": total_resources["total_cores"],
                "cluster_usage": {
                    "memory_used": used_resources["memory_used"],
                    "cores_used": used_resources["cores_used"],
                },
                "attached_codespaces_count": attached_codespaces_count,
                "ray_dashboard": parse_url(cluster["dashboards"]["data"]["ray_dashboard_url"]),
                "prometheus_dashboard": parse_url(cluster["dashboards"]["data"]["grafana_dashboard_url"]),
                "spark_ui_dashboard": parse_url(cluster["dashboards"]["data"]["spark_ui_url"]),
            },
        }
    except Exception as e:
        logger.error(f"Error in launch_imported_project: {e}")
        return error_handler(str(e))
