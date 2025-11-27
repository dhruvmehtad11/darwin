from workspace_app_layer.controllers.workspace.check_unique import get_git_project_details
from workspace_app_layer.models.workspace.import_project_request import ImportProjectRequest
from workspace_app_layer.utils.utils import error_handler, get_active_resources, get_total_resources, parse_url
from workspace_core.entities.codespace import Codespace
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger
from workspace_core.workspaces import WorkspacesSDK

logger = get_logger(__name__)


def import_project(workspace: WorkspacesSDK, request: ImportProjectRequest, compute: Compute, env):
    try:
        git_project_details = get_git_project_details(request.github_url)
        github_url = (
            "https://" + "github.com/" + git_project_details["user"] + "/" + git_project_details["project"] + ".git"
        )

        import_project_resp = workspace.import_project(request.user, github_url)
        logger.debug(f"Import project response for {request.github_url}: {import_project_resp}")
        project_id = import_project_resp["project_id"]
        project_name = import_project_resp["name"]

        formatted_cs_name = request.codespace_name.replace(" ", "-")
        codespace = Codespace(project_id, formatted_cs_name, user=request.user)

        try:
            codespace_id = workspace.create_codespace(codespace)["codespace_id"]
            logger.debug(f"Codespace created with id for project {project_id}: {codespace_id}")
        except Exception as err:
            logger.error(f"Error while creating codespace for project {project_name} : {str(err)}")
            workspace.delete_project_from_db(int(project_id))
            raise Exception(err.__str__())

        try:
            workspace.attach_cluster(codespace_id, request.cluster_id)
        except Exception as err:
            logger.error(f"Error while attaching cluster to codespace {codespace_id} : {str(err)}")
            workspace.delete_codespace_from_db(int(codespace_id))
            workspace.delete_project_from_db(int(project_id))
            raise Exception(err.__str__())

        cluster_details = compute.get_cluster_details(request.cluster_id)
        logger.debug(f"Cluster details for {request.cluster_id}: {cluster_details}")

        total_resources = get_total_resources(
            cluster_details["head_node_config"], cluster_details["worker_node_configs"]
        )
        logger.debug(f"Total resources for cluster {request.cluster_id}: {total_resources}")

        attached_codespaces_count = workspace.attached_codespaces_count(request.cluster_id)

        try:
            launch_codespace_resp = workspace.launch_imported_project_v2(
                codespace_id, github_link=github_url, user_id=request.user
            )
            logger.debug(f"Launch codespace response for {codespace_id}: {launch_codespace_resp}")
        except Exception as err:
            logger.error(f"Error while launching codespace {codespace_id} : {str(err)}")
            workspace.delete_codespace_from_db(codespace_id)
            workspace.delete_project_from_db(project_id)
            raise Exception(err.__str__())

        # If cluster is inactive
        if cluster_details["status"] != "active":
            if cluster_details["status"] == "inactive":
                compute.start_cluster(cluster_details["cluster_id"], request.user)
            return {
                "project_id": project_id,
                "project_name": project_name,
                "codespace_id": codespace_id,
                "codespace_name": formatted_cs_name,
                "github_link": github_url,
                "jupyter_lab_link": None,
                "code_server_link": None,
                "attached_cluster": {
                    "cluster_id": cluster_details["cluster_id"],
                    "cluster_name": cluster_details["name"],
                    "cluster_status": "creating",
                    "memory": total_resources["total_memory"],
                    "cores": total_resources["total_cores"],
                    "cluster_usage": {"memory_used": 0, "cores_used": 0},
                    "attached_codespaces_count": attached_codespaces_count,
                    "ray_dashboard": None,
                    "prometheus_dashboard": None,
                },
            }
        else:
            used_resources = get_active_resources(compute, cluster_details["cluster_id"])
            logger.debug(f"Used resources for cluster {request.cluster_id}: {used_resources}")
            return {
                "project_id": project_id,
                "project_name": project_name,
                "codespace_id": codespace_id,
                "codespace_name": formatted_cs_name,
                "github_link": github_url,
                "jupyter_lab_link": parse_url(launch_codespace_resp["jupyter_link"]),
                "code_server_link": parse_url(launch_codespace_resp["code_server_link"]),
                "attached_cluster": {
                    "cluster_id": cluster_details["cluster_id"],
                    "cluster_name": cluster_details["name"],
                    "cluster_status": "active",
                    "memory": total_resources["total_memory"],
                    "cores": total_resources["total_cores"],
                    "cluster_usage": {
                        "memory_used": used_resources["memory_used"],
                        "cores_used": used_resources["cores_used"],
                    },
                    "attached_codespaces_count": attached_codespaces_count,
                    "ray_dashboard": parse_url(cluster_details["dashboards"]["data"]["ray_dashboard_url"]),
                    "prometheus_dashboard": parse_url(cluster_details["dashboards"]["data"]["grafana_dashboard_url"]),
                    "spark_ui_dashboard": parse_url(cluster_details["dashboards"]["data"]["spark_ui_url"]),
                },
            }

    except Exception as err:
        logger.error(f"Error while importing project {request.github_url}: {str(err)}")
        return error_handler(f"Project not imported successfully {str(err)}")
