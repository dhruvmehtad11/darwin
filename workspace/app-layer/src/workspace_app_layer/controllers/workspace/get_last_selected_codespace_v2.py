from workspace_app_layer.controllers.workspace.utils.jupyter_utils import get_jupyter_suffix, get_code_server_link
from workspace_app_layer.utils.utils import (
    error_handler,
    get_total_resources,
    get_active_resources,
    serialize_date,
    parse_url,
)
from workspace_core.entities.codespace import Codespace
from workspace_core.entities.project import Project
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger
from workspace_core.workspaces import WorkspacesSDK
from workspace_app_layer.constants.config import Config as AppConfig

logger = get_logger(__name__)


async def get_last_selected_codespace_by_user_v2_controller(
    workspace: WorkspacesSDK, user_id: str, compute: Compute, env
):
    try:
        codespace = workspace.last_selected_codespace(user_id)
        logger.debug(f"Last selected codespace for user {user_id}: {codespace}")

        if not codespace:
            default_project = Project(user_id=user_id, name="Playground")
            create_project_resp = workspace.create_project(default_project)
            logger.debug(f"Created Playground for user {user_id}: {create_project_resp}")
            project_id = create_project_resp["project_id"]

            default_codespace = Codespace(project_id=project_id, name="default", user=user_id)
            create_codespace_resp = workspace.create_codespace(default_codespace)
            logger.debug(f"Created default codespace for user {user_id}: {create_codespace_resp}")
            codespace_id = create_codespace_resp["codespace_id"]

            try:
                cluster = compute.get_cluster_details(AppConfig(env).playground_cluster)

                workspace.attach_cluster(codespace_id, AppConfig(env).playground_cluster)

                total_resources = get_total_resources(cluster["head_node_config"], cluster["worker_node_configs"])

                attached_codespaces_count = workspace.attached_codespaces_count(AppConfig(env).playground_cluster)

                launch_codespace_resp = workspace.launch_codespace_v2(codespace_id, user_id=user_id, cloned_from=None)

                used_resources = get_active_resources(compute, cluster["cluster_id"])
                logger.debug(f"Used resources for cluster {cluster['cluster_id']}: {used_resources}")
            except Exception as e:
                logger.error(f"Failed to launch codespace {codespace_id}", exc_info=True)
                workspace.delete_codespace_from_db(int(codespace_id))
                workspace.delete_project_from_db(int(project_id))
                raise Exception(e.__str__())

            return {
                "project_id": create_project_resp["project_id"],
                "project_name": create_project_resp["name"],
                "codespace_id": create_codespace_resp["codespace_id"],
                "codespace_name": create_codespace_resp["name"],
                "github_link": None,
                "last_synced_at": serialize_date(launch_codespace_resp["codespace"].last_synced_at),
                "jupyter_lab_link": parse_url(launch_codespace_resp["jupyter_link"]),
                "code_server_link": parse_url(launch_codespace_resp["code_server_link"]),
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
                },
            }

        jupyter_suffix = get_jupyter_suffix(
            env=env, user_id=user_id, project_name=codespace["project_name"], codespace_name=codespace["name"]
        )
        codespace["code_server_link"] = None

        # If Cluster is attached or not
        if codespace["cluster_id"]:
            cluster = compute.get_cluster_details(codespace["cluster_id"])
            logger.debug(f"Cluster details for cluster {codespace['cluster_id']}: {cluster}")

            total_resources = get_total_resources(cluster["head_node_config"], cluster["worker_node_configs"])
            logger.debug(f"Total resources for cluster {codespace['cluster_id']}: {total_resources}")

            attached_codespaces_count = workspace.attached_codespaces_count(codespace["cluster_id"])
            used_resources = get_active_resources(compute, codespace["cluster_id"])
            logger.debug(f"Used resources for cluster {codespace['cluster_id']}: {used_resources}")

            codespace["code_server_link"] = (
                f'{cluster["dashboards"]["data"]["code_server_url"]}?{get_code_server_link(env=env, user_id=user_id, project_name=codespace["project_name"], codespace_name=codespace["name"])}'
            )

            attached_cluster = {
                "cluster_id": codespace["cluster_id"],
                "cluster_name": codespace["cluster"]["name"],
                "cluster_status": codespace["cluster"]["status"],
                "memory": total_resources["total_memory"],
                "cores": total_resources["total_cores"],
                "cluster_usage": {
                    "memory_used": used_resources["memory_used"],
                    "cores_used": used_resources["cores_used"],
                },
                "attached_codespaces_count": attached_codespaces_count,
                "ray_dashboard": parse_url(cluster["dashboards"]["data"]["ray_dashboard_url"]),
                "spark_ui_dashboard": parse_url(cluster["dashboards"]["data"]["spark_ui_url"]),
                "prometheus_dashboard": None,
            }

            # If cluster is inactive
            if cluster["status"] != "active":
                attached_cluster["cluster_status"] = cluster["status"]
                attached_cluster["cluster_usage"]["memory_used"] = 0
                attached_cluster["cluster_usage"]["cores_used"] = 0
                attached_cluster["ray_dashboard"] = None
                codespace["code_server_link"] = None
                codespace["jupyter_link"] = compute.get_jupyter_client(
                    workspace_id=codespace["project_id"], codespace_id=codespace["id"], suffix=jupyter_suffix
                )
        else:
            codespace["jupyter_link"] = compute.get_jupyter_client(
                workspace_id=codespace["project_id"], codespace_id=codespace["id"], suffix=jupyter_suffix
            )
            attached_cluster = None

    except Exception as err:
        logger.error("Failed to get last selected codespace", exc_info=True)
        return error_handler(err.__str__())

    get_last_selected_codespace_response = {
        "status": "SUCCESS",
        "data": {
            "project_id": codespace["project_id"],
            "project_name": codespace["project_name"],
            "codespace_id": codespace["id"],
            "codespace_name": codespace["name"],
            "github_link": codespace["cloned_from"],
            "last_sync_time": serialize_date(codespace["last_synced_at"]),
            "jupyter_lab_link": parse_url(codespace["jupyter_link"]),
            "code_server_link": parse_url(codespace["code_server_link"]),
            "attached_cluster": attached_cluster,
        },
    }
    return get_last_selected_codespace_response
