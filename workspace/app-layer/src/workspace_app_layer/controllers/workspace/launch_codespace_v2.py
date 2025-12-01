from fastapi.responses import JSONResponse

from workspace_app_layer.controllers.workspace.utils.launch_strategy import LaunchStrategyFactory
from workspace_app_layer.models.workspace.launch_codespace_request import LaunchCodespaceRequest
from workspace_app_layer.utils.response_utils import Response
from workspace_core.dto.response import CodespaceResponse, ProjectResponse
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


async def launch_codespace_v3_controller(
    workspace, request: LaunchCodespaceRequest, compute: Compute, env
) -> JSONResponse:
    try:
        logger.info(f"Launch codespace request received: {request}")
        workspace.insert_last_selected_codespace(request.codespace_id, user_id=request.user)
        codespace_details: CodespaceResponse = workspace.codespace_details(request.codespace_id)
        project_details: ProjectResponse = workspace.project_details(request.project_id)

        try:
            cluster = compute.get_cluster_details(codespace_details.cluster_id)
            logger.debug(
                f"Cluster details for codespace_id - {codespace_details.id} & cluster_id {codespace_details.cluster_id}: {cluster}"
            )
        except Exception as err:
            logger.error(
                f"Error in getting cluster details of cluster with id {codespace_details.cluster_id} with error {err}"
            )
            workspace.detach_cluster(request.codespace_id)
            return Response.response(
                "Codespace launched successfully",
                LaunchStrategyFactory.get_strategy(None).handle(
                    workspace=workspace,
                    compute=compute,
                    codespace_details=codespace_details,
                    project_details=project_details,
                    env=env,
                    user=request.user,
                ),
            )

        logger.info(
            f"Launching codespace for project_id: {request.project_id} and codespace_id: {request.codespace_id}"
        )

        return Response.response(
            "Codespace launched successfully",
            LaunchStrategyFactory.get_strategy(cluster["status"]).handle(
                workspace=workspace,
                compute=compute,
                codespace_details=codespace_details,
                project_details=project_details,
                env=env,
                cluster=cluster,
                user=request.user,
            ),
        )
    except Exception as e:
        import traceback

        traceback.print_exc()
        logger.error(f"Error in launch_codespace_v2_controller: {e}")
        return Response.internal_server_error_response(f"Error in launching codespace: {request.codespace_id}", str(e))
