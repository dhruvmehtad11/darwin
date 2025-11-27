import asyncio
import json
import unittest
from unittest.mock import patch

from workspace_app_layer.controllers.workspace.get_workspaces_and_codespaces import (
    get_workspaces_and_codespaces_by_cluster_id,
)
from workspace_core.dto.response.workspace_and_codespace_response import WorkspaceAndCodespaceResponse
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


class TestGetWorkspacesAndCodespacesByClusterId(unittest.TestCase):
    @patch("workspace_core.workspaces.WorkspacesSDK")
    def setUp(self, mock_workspace_sdk):
        self.mock_workspace_sdk = mock_workspace_sdk.return_value
        self.workspace_and_codespace_response = WorkspaceAndCodespaceResponse(
            project_id=1, project_name="test_project", codespace_id=1, codespace_name="test_codespace"
        )

    async def test_get_workspaces_and_codespaces_by_cluster_id_success(self):
        cluster_id = "test_id"
        self.mock_workspace_sdk.list_workspaces_and_codespaces.return_value = [self.workspace_and_codespace_response]
        resp = await get_workspaces_and_codespaces_by_cluster_id(self.mock_workspace_sdk, cluster_id)

        resp = json.loads(resp.body)
        self.assertEqual(resp["data"][0]["project_id"], self.workspace_and_codespace_response.project_id)
        self.assertEqual(resp["data"][0]["project_name"], self.workspace_and_codespace_response.project_name)
        self.assertEqual(resp["data"][0]["codespace_id"], self.workspace_and_codespace_response.codespace_id)
        self.assertEqual(resp["data"][0]["codespace_name"], self.workspace_and_codespace_response.codespace_name)

    async def test_get_workspaces_and_codespaces_by_cluster_id_exception(self):
        cluster_id = "test_id"
        expected = "Error in getting Workspaces and Codespaces for Cluster Id"
        self.mock_workspace_sdk.list_workspaces_and_codespaces.side_effect = Exception(expected)
        resp = await get_workspaces_and_codespaces_by_cluster_id(self.mock_workspace_sdk, cluster_id)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], f"Error in getting Workspaces and Codespaces for Cluster Id {cluster_id} ")

    def run_async_test(self, coro):
        return asyncio.get_event_loop().run_until_complete(coro)

    def test_all(self):
        self.run_async_test(self.test_get_workspaces_and_codespaces_by_cluster_id_success())
        self.run_async_test(self.test_get_workspaces_and_codespaces_by_cluster_id_exception())
