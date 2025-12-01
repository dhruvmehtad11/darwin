from datetime import datetime

from workspace_app_layer.controllers.workspace.utils.jupyter_utils import get_jupyter_suffix
from workspace_app_layer.models.workspace.create_codespace_request import CreateCodespaceRequest
from workspace_app_layer.utils.utils import (
    error_handler,
    get_total_resources,
    get_active_resources,
    serialize_date,
    parse_url,
)
from workspace_core.dto.response import ProjectResponse, CodespaceResponse
from workspace_core.entities.codespace import Codespace
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger
from workspace_core.workspaces import WorkspacesSDK

logger = get_logger(__name__)


async def create_codespace_v2_controller(
    workspace: WorkspacesSDK, request: CreateCodespaceRequest, compute: Compute, env
):
    try:
        project: ProjectResponse = workspace.project_details(int(request.project_id))

        last_synced_at = (
            datetime.now().replace(microsecond=0)
            if not request.clone_from_codespace_name
            else workspace.get_codespace_from_name(
                request.clone_from_codespace_name, int(request.project_id)
            ).last_synced_at
        )

        formatted_cs_name = request.codespace_name.replace(" ", "-")
        codespace = Codespace(project_id=int(request.project_id), name=formatted_cs_name, user=request.user)
        create_codespace_resp = workspace.create_codespace(codespace)
        logger.debug(f"Created codespace: {create_codespace_resp}")
        codespace_id = create_codespace_resp["codespace_id"]

        try:
            if request.cluster_id is not None:
                attach_cluster_resp = workspace.attach_cluster(codespace_id, request.cluster_id)
        except Exception as e:
            workspace.delete_codespace_from_db(int(codespace_id))
            logger.error(f"Failed to attach cluster {request.cluster_id} to codespace {codespace_id}")
            raise e

        attached_cluster, cluster, total_resources, attached_codespaces_count = None, None, None, None
        if request.cluster_id is not None:
            cluster = compute.get_cluster_details(request.cluster_id)
            logger.debug(f"Cluster details for {request.cluster_id}: {cluster}")
            total_resources = get_total_resources(cluster["head_node_config"], cluster["worker_node_configs"])
            logger.debug(f"Total resources for cluster {request.cluster_id}: {total_resources}")
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
            launch_codespace_resp = workspace.launch_codespace_v2(
                codespace_id, user_id=request.user, cloned_from=request.clone_from_codespace_name
            )
        except Exception as e:
            workspace.delete_codespace_from_db(int(codespace_id))
            logger.error(f"Error while launching codespace {codespace_id}: {e}")
            raise Exception(f"Error while launching codespace: {str(e)}")

        if request.cluster_id is None or cluster["status"] != "active":
            jupyter_suffix = get_jupyter_suffix(
                env=env, user_id=project.user_id, project_name=project.name, codespace_name=formatted_cs_name
            )
            jupyter_lab_link = compute.get_jupyter_client(
                workspace_id=int(request.project_id), codespace_id=codespace_id, suffix=jupyter_suffix
            )
            if request.cluster_id is not None and cluster["status"] == "inactive":
                compute.start_cluster(cluster["cluster_id"], request.user)

            return {
                "project_id": request.project_id,
                "project_name": project.name,
                "codespace_id": codespace_id,
                "codespace_name": formatted_cs_name,
                "github_link": project.cloned_from,
                "jupyter_lab_link": jupyter_lab_link,
                "code_server_link": None,
                "attached_cluster": attached_cluster,
            }

    except Exception as e:
        import traceback

        traceback.print_exc()
        return error_handler(f"Cannot create codespace {codespace_id} - {e}")

    try:
        used_resources = get_active_resources(compute, request.cluster_id)
        logger.debug(f"Used resources for cluster {request.cluster_id}: {used_resources}")

        success_response = {
            "status": "SUCCESS",
            "project_id": request.project_id,
            "project_name": launch_codespace_resp["project"].name,
            "codespace_id": codespace_id,
            "codespace_name": formatted_cs_name,
            "github_link": launch_codespace_resp["project"].cloned_from,
            "last_sync_time": serialize_date(last_synced_at),
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
    except Exception as e:
        return error_handler(f"Cannot create codespace because. - {e}")
