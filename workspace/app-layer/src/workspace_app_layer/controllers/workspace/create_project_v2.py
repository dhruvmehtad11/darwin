from workspace_app_layer.controllers.workspace.utils.jupyter_utils import get_jupyter_suffix
from workspace_app_layer.models.workspace.create_project_request import CreateProjectRequest
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

logger = get_logger(__name__)


def create_project_v2_controller(workspace: WorkspacesSDK, request: CreateProjectRequest, compute: Compute, env):
    try:
        formatted_project_name = request.project_name.replace(" ", "-")
        project = Project(request.user, formatted_project_name)
        create_project_resp = workspace.create_project(project)
        logger.debug(f"Project created successfully - {create_project_resp}")
        project_id = create_project_resp["project_id"]

        formatted_cs_name = request.codespace_name.replace(" ", "-")
        codespace = Codespace(project_id, formatted_cs_name, request.user)
        try:
            codespace_id = workspace.create_codespace(codespace)["codespace_id"]
            logger.debug(f"Codespace created successfully - {codespace_id}")
        except Exception as e:
            logger.error(f"Failed to create codespace for project {request.project_name}")
            workspace.delete_project_from_db(int(project_id))
            raise e

        try:
            if request.cluster_id is not None:
                workspace.attach_cluster(codespace_id, request.cluster_id)
        except Exception as e:
            logger.error(f"Failed to attach cluster for codespace {codespace_id}")
            workspace.delete_codespace_from_db(int(codespace_id))
            workspace.delete_project_from_db(int(project_id))
            raise e

        attached_cluster, cluster, total_resources, attached_codespaces_count = None, None, None, None
        if request.cluster_id is not None:
            cluster = compute.get_cluster_details(request.cluster_id)
            logger.debug(f"Cluster details - {cluster}")
            total_resources = get_total_resources(cluster["head_node_config"], cluster["worker_node_configs"])
            attached_codespaces_count = workspace.attached_codespaces_count(request.cluster_id)
            attached_cluster = {
                "cluster_id": request.cluster_id,
                "cluster_name": cluster["name"],
                "cluster_status": "creating",
                "memory": total_resources["total_memory"],
                "cores": total_resources["total_cores"],
                "cluster_usage": {"memory_used": 0, "cores_used": 0},
                "attached_codespaces_count": attached_codespaces_count,
                "ray_dashboard": None,
                "prometheus_dashboard": None,
            }

        try:
            launch_codespace_resp = workspace.launch_codespace_v2(codespace_id, user_id=request.user, cloned_from=None)
        except Exception as e:
            logger.error(f"Failed to launch codespace {codespace_id}")
            workspace.delete_codespace_from_db(int(codespace_id))
            workspace.delete_project_from_db(int(project_id))
            raise e

        if request.cluster_id is None or cluster["status"] != "active":
            jupyter_suffix = get_jupyter_suffix(
                env=env, user_id=request.user, project_name=project.name, codespace_name=formatted_cs_name
            )
            jupyter_lab_link = compute.get_jupyter_client(
                workspace_id=int(project_id), codespace_id=codespace_id, suffix=jupyter_suffix
            )
            if request.cluster_id is not None and cluster["status"] == "inactive":
                compute.start_cluster(cluster["cluster_id"], request.user)

            return {
                "project_id": project_id,
                "project_name": formatted_project_name,
                "codespace_id": codespace_id,
                "codespace_name": formatted_cs_name,
                "github_link": None,
                "jupyter_lab_link": jupyter_lab_link,
                "code_server_link": None,
                "attached_cluster": attached_cluster,
            }

    except Exception as err:
        return error_handler(f"Project not created successfully - {err}")

    try:
        used_resources = get_active_resources(compute, request.cluster_id)

        success_response = {
            "project_id": create_project_resp["project_id"],
            "project_name": create_project_resp["name"],
            "codespace_id": codespace_id,
            "codespace_name": formatted_cs_name,
            "github_link": create_project_resp["cloned_from"],
            "last_sync_time": serialize_date(launch_codespace_resp["codespace"].last_synced_at),
            "jupyter_lab_link": parse_url(launch_codespace_resp["jupyter_link"]),
            "code_server_link": parse_url(launch_codespace_resp["code_server_link"]),
            "attached_cluster": {
                "cluster_id": request.cluster_id,
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
        return success_response
    except Exception as err:
        return error_handler(f"Project not created successfully - {err}")
