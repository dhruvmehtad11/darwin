from abc import ABC, abstractmethod
from typing import Optional

from workspace_app_layer.controllers.workspace.utils.jupyter_utils import get_jupyter_suffix
from workspace_app_layer.models.workspace.response.launch_codespace_response import LaunchCodespaceResponse
from workspace_app_layer.utils.response_mapper import attached_cluster_details
from workspace_app_layer.utils.utils import parse_url
from workspace_core.dto.response import CodespaceResponse, ProjectResponse
from workspace_core.service.compute import Compute
from workspace_core.utils.logging_util import get_logger
from workspace_core.workspaces import WorkspacesSDK

logger = get_logger(__name__)


class LaunchStrategy(ABC):
    @abstractmethod
    async def handle(
        self,
        workspace: WorkspacesSDK,
        compute: Compute,
        env,
        codespace_details: CodespaceResponse,
        project_details: ProjectResponse,
        user: str,
        cluster: dict = None,
        cloned_from: Optional[str] = None,
    ):
        pass


class ActiveClusterStrategy(LaunchStrategy):
    def handle(
        self,
        workspace: WorkspacesSDK,
        compute: Compute,
        env: str,
        codespace_details: CodespaceResponse,
        project_details: ProjectResponse,
        user: str,
        cluster: dict = None,
        cloned_from: Optional[str] = None,
    ):
        logger.debug(
            f"Launching codespace for project_id: {project_details.id} and codespace_id: {codespace_details.id} with cluster: {cluster}"
        )
        # Implementation for active cluster
        launch_codespace = workspace.launch_codespace_v2(
            codespace_id=codespace_details.id, user_id=user, cloned_from=None
        )

        return LaunchCodespaceResponse(
            project_id=project_details.id,
            project_name=project_details.name,
            codespace_id=codespace_details.id,
            codespace_name=codespace_details.name,
            github_link=project_details.cloned_from,
            attached_cluster=attached_cluster_details(cluster, compute, workspace),
            code_server_link=parse_url(launch_codespace["code_server_link"]),
            jupyter_lab_link=parse_url(launch_codespace["jupyter_link"]),
        )


class InactiveClusterStrategy(LaunchStrategy):
    def handle(
        self,
        workspace: WorkspacesSDK,
        compute: Compute,
        env: str,
        codespace_details: CodespaceResponse,
        project_details: ProjectResponse,
        user: str,
        cluster: dict = None,
        cloned_from: Optional[str] = None,
    ):
        # Implementation for inactive cluster
        launch_codespace = workspace.launch_codespace_v2(
            codespace_id=codespace_details.id, user_id=user, cloned_from=None
        )

        logger.debug(
            f"Launching codespace for project_id: {project_details.id} and codespace_id: {codespace_details.id} with cluster: {cluster}"
        )
        return LaunchCodespaceResponse(
            project_id=project_details.id,
            project_name=project_details.name,
            codespace_id=codespace_details.id,
            codespace_name=codespace_details.name,
            github_link=project_details.cloned_from,
            jupyter_lab_link=compute.get_jupyter_client(
                workspace_id=project_details.id,
                codespace_id=codespace_details.id,
                suffix=get_jupyter_suffix(
                    env=env,
                    user_id=project_details.user_id,
                    project_name=project_details.name,
                    codespace_name=codespace_details.name,
                ),
            ),
            attached_cluster=attached_cluster_details(cluster, compute, workspace),
        )


class NoClusterStrategy(LaunchStrategy):
    def handle(
        self,
        workspace: WorkspacesSDK,
        compute: Compute,
        env: str,
        codespace_details: CodespaceResponse,
        project_details: ProjectResponse,
        user: str,
        cluster: dict = None,
        cloned_from: Optional[str] = None,
    ):
        # Implementation when no cluster is attached
        launch_codespace = workspace.launch_codespace_v2(
            codespace_id=codespace_details.id, user_id=user, cloned_from=None
        )
        logger.debug(
            f"Launching codespace for project_id: {project_details.id} and codespace_id: {codespace_details.id}"
        )
        return LaunchCodespaceResponse(
            project_id=project_details.id,
            project_name=project_details.name,
            codespace_id=codespace_details.id,
            codespace_name=codespace_details.name,
            github_link=project_details.cloned_from,
            jupyter_lab_link=compute.get_jupyter_client(
                workspace_id=project_details.id,
                codespace_id=codespace_details.id,
                suffix=get_jupyter_suffix(
                    env=env,
                    user_id=project_details.user_id,
                    project_name=project_details.name,
                    codespace_name=codespace_details.name,
                ),
            ),
            attached_cluster=None,
        )


class LaunchStrategyFactory:
    @staticmethod
    def get_strategy(cluster_status):
        logger.debug(f"Cluster status: {cluster_status}")
        if cluster_status is None:
            return NoClusterStrategy()
        elif cluster_status == "active":
            return ActiveClusterStrategy()
        elif cluster_status == "inactive" or cluster_status == "creating":
            return InactiveClusterStrategy()
        else:
            raise ValueError("Invalid cluster status")
