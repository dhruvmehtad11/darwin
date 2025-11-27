from workspace_app_layer.controllers.workspace.utils.jupyter_utils import get_jupyter_suffix
from workspace_app_layer.models.workspace.attach_cluster_request import AttachClusterRequest
from workspace_app_layer.utils.utils import (
    error_handler,
    get_total_resources,
    get_active_resources,
    serialize_date,
    parse_url,
)
from workspace_core.workspaces import WorkspacesSDK
from workspace_core.dto.response import ProjectResponse, CodespaceResponse
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


async def attach_cluster_v2_controller(workspace: WorkspacesSDK, request: AttachClusterRequest, compute: Compute, env):
    try:
        project: ProjectResponse = workspace.project_details(request.project_id)
        codespace: CodespaceResponse = workspace.codespace_details(request.codespace_id)
        cluster = compute.get_cluster_details(request.cluster_id)
        logger.debug(f"Cluster Details for project {request.project_id}, codespace {request.codespace_id}: {cluster}")

        attach_cluster_response = workspace.attach_cluster(request.codespace_id, request.cluster_id)
        if attach_cluster_response == 0:
            raise Exception("Cluster Already Attached")

        total_resources = get_total_resources(cluster["head_node_config"], cluster["worker_node_configs"])
        logger.debug(
            f"Total Resources for project {request.project_id}, codespace {request.codespace_id}, cluster {cluster}: {total_resources}"
        )

        attached_codespaces_count = workspace.attached_codespaces_count(request.cluster_id)

        if cluster["status"] != "active":
            if cluster["status"] == "inactive":
                compute.start_cluster(request.cluster_id, request.user)
                logger.debug(f"Cluster {request.cluster_id} started")

            jupyter_suffix = get_jupyter_suffix(
                env=env, user_id=project.user_id, project_name=project.name, codespace_name=codespace.name
            )
            logger.debug(
                f"Jupyter Suffix for project {request.project_id}, codespace {request.codespace_id}: {jupyter_suffix}"
            )

            jupyter_lab_link = compute.get_jupyter_client(
                workspace_id=request.project_id, codespace_id=request.codespace_id, suffix=jupyter_suffix
            )

            logger.debug(
                f"Jupyter Lab Link for project {request.project_id}, codespace {request.codespace_id}: {jupyter_lab_link}"
            )
            return {
                "project_id": project.id,
                "project_name": project.name,
                "codespace_id": request.codespace_id,
                "codespace_name": codespace.name,
                "github_link": project.cloned_from,
                "jupyter_lab_link": jupyter_lab_link,
                "code_server_link": None,
                "attached_cluster": {
                    "cluster_id": request.cluster_id,
                    "cluster_name": cluster["name"],
                    "cluster_status": "creating",
                    "memory": total_resources["total_memory"],
                    "cores": total_resources["total_cores"],
                    "cluster_usage": {"memory_used": 0, "cores_used": 0},
                    "attached_codespaces_count": attached_codespaces_count,
                    "ray_dashboard": None,
                    "prometheus_dashboard": None,
                },
            }

        launch_codespace_response = workspace.launch_codespace_v2(
            request.codespace_id, user_id=request.user, cloned_from=None
        )
    except Exception as err:
        logger.error(f"Error while attaching cluster for request {request}: {err}")
        return error_handler(err.__str__())

    used_resources = get_active_resources(compute, request.cluster_id)
    logger.debug(
        f"Used Resources for project {request.project_id}, codespace {request.codespace_id}, cluster {cluster}: {used_resources}"
    )

    attach_cluster_response = {
        "project_id": project.id,
        "project_name": project.name,
        "codespace_id": request.codespace_id,
        "codespace_name": codespace.name,
        "github_link": project.cloned_from,
        "last_sync_time": serialize_date(codespace.last_synced_at),
        "jupyter_lab_link": parse_url(launch_codespace_response["jupyter_link"]),
        "code_server_link": parse_url(launch_codespace_response["code_server_link"]),
        "attached_cluster": {
            "cluster_id": request.cluster_id,
            "cluster_name": cluster["name"],
            "cluster_status": cluster["status"],
            "memory": total_resources["total_memory"],
            "cores": total_resources["total_cores"],
            "cluster_usage": {"memory_used": used_resources["memory_used"], "cores_used": used_resources["cores_used"]},
            "attached_codespaces_count": attached_codespaces_count,
            "ray_dashboard": parse_url(cluster["dashboards"]["data"]["ray_dashboard_url"]),
            "prometheus_dashboard": parse_url(cluster["dashboards"]["data"]["grafana_dashboard_url"]),
            "spark_ui_dashboard": parse_url(cluster["dashboards"]["data"]["spark_ui_url"]),
        },
    }
    return attach_cluster_response
