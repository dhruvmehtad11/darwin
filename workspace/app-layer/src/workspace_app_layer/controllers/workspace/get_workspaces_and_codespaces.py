from workspace_app_layer.utils.response_utils import Response
from workspace_core.dto.response.workspace_and_codespace_response import WorkspaceAndCodespaceResponse
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


async def get_workspaces_and_codespaces_by_cluster_id(workspace, cluster_id: str):
    try:
        resp: list[WorkspaceAndCodespaceResponse] = workspace.list_workspaces_and_codespaces(cluster_id)
        logger.info(f"Workspaces and Codespaces for Cluster Id {cluster_id}: {resp}")

        workspaces_and_codespaces_response = []

        for item in resp:
            workspaces_and_codespaces_response.append(item)

        return Response.success_response(
            "Workspaces and Codespaces data fetched successfully", workspaces_and_codespaces_response
        )

    except Exception as e:
        logger.error(f"Error in getting Workspaces and Codespaces for Cluster Id {cluster_id}")
        return Response.internal_server_error_response(
            f"Error in getting Workspaces and Codespaces for Cluster Id {cluster_id} ", e.__str__()
        )
