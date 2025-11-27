import asyncio
import json
import unittest
from unittest.mock import patch

import pytest

from workspace_app_layer.controllers.workspace.launch_codespace_v2 import launch_codespace_v3_controller
from workspace_app_layer.models.workspace import LaunchCodespaceRequest


class TestLaunchCodespaceV2(unittest.TestCase):
    @patch("workspace_core.workspaces.WorkspacesSDK")
    @patch("workspace_core.service.compute.Compute")
    def setUp(self, mock_compute, mock_workspace):
        self.mock_compute = mock_compute.return_value
        self.mock_workspace = mock_workspace.return_value
        self.mock_workspace.insert_last_selected_codespace.return_value = (1, None)
        self.mock_workspace.codespace_details.return_value = self.codespace_details
        self.mock_workspace.project_details.return_value = self.project_details
        self.mock_compute.get_cluster_details.return_value = self.compute_cluster_details
        self.request = LaunchCodespaceRequest(project_id=1, codespace_id=1, user="test")

    @pytest.fixture(autouse=True)
    def prepare_fixture(self, codespace_details, project_details, compute_cluster_details):
        self.codespace_details = codespace_details
        self.project_details = project_details
        self.compute_cluster_details = compute_cluster_details

    async def test_launch_codespace_v3_controller_success(self):
        resp = await launch_codespace_v3_controller(
            workspace=self.mock_workspace, request=self.request, compute=self.mock_compute, env="darwin-local"
        )
        resp = json.loads(resp.body)
        self.assertEqual(resp["project_id"], 1)
        self.assertEqual(resp["project_name"], "test")
        self.assertEqual(resp["codespace_id"], 1)
        self.assertEqual(resp["codespace_name"], "test")

    async def test_launch_codespace_v3_controller_cluster_exception(self):
        self.mock_compute.get_cluster_details.side_effect = Exception("")

        resp = await launch_codespace_v3_controller(
            workspace=self.mock_workspace, request=self.request, compute=self.mock_compute, env="darwin-local"
        )
        resp = json.loads(resp.body)
        self.assertEqual(resp["project_id"], 1)
        self.assertEqual(resp["project_name"], "test")
        self.assertEqual(resp["codespace_id"], 1)
        self.assertEqual(resp["codespace_name"], "test")
        self.assertEqual(resp["attached_cluster"], None)

    async def test_launch_codespace_v3_controller_exception(self):
        self.mock_workspace.codespace_details.side_effect = Exception("")

        resp = await launch_codespace_v3_controller(
            workspace=self.mock_workspace, request=self.request, compute=self.mock_compute, env="darwin-local"
        )
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "Error in launching codespace: 1")

    def run_async_test(self, coro):
        return asyncio.get_event_loop().run_until_complete(coro)

    def test_all(self):
        self.run_async_test(self.test_launch_codespace_v3_controller_success())
        self.run_async_test(self.test_launch_codespace_v3_controller_cluster_exception())
        self.run_async_test(self.test_launch_codespace_v3_controller_exception())
