import unittest
from unittest.mock import patch

import pytest

from workspace_app_layer.controllers.workspace.utils.launch_strategy import LaunchStrategyFactory
from workspace_app_layer.models.workspace.response.launch_codespace_response import LaunchCodespaceResponse


class TestLaunchStrategy(unittest.TestCase):
    @patch("workspace_core.workspaces.WorkspacesSDK")
    @patch("workspace_core.service.compute.Compute")
    def setUp(self, mock_compute, mock_workspace):
        self.mock_compute = mock_compute.return_value
        self.mock_workspace = mock_workspace.return_value
        self.mock_compute.get_jupyter_client.return_value = "http://localhost:8888/fsx/workspace/test/test/test"
        self.mock_workspace.attached_codespaces_count.return_value = 1
        self.mock_compute.get_cluster_dashboards.return_value = self.compute_cluster_details["dashboards"]["data"]

    @pytest.fixture(autouse=True)
    def prepare_fixture(self, codespace_details, project_details, compute_cluster_details):
        self.codespace_details = codespace_details
        self.project_details = project_details
        self.compute_cluster_details = compute_cluster_details

    def test_no_cluster_attached_strategy(self):
        resp = (
            LaunchStrategyFactory()
            .get_strategy(None)
            .handle(
                workspace=self.mock_workspace,
                compute=self.mock_compute,
                env="darwin-local",
                codespace_details=self.codespace_details,
                project_details=self.project_details,
                user="test",
            )
        )

        self.assertIsInstance(resp, LaunchCodespaceResponse)
        self.assertEqual(resp.attached_cluster, None)

    def test_inactive_cluster_strategy(self):
        resp = (
            LaunchStrategyFactory()
            .get_strategy("inactive")
            .handle(
                workspace=self.mock_workspace,
                compute=self.mock_compute,
                env="darwin-local",
                codespace_details=self.codespace_details,
                project_details=self.project_details,
                user="test",
                cluster=self.compute_cluster_details,
            )
        )

        self.assertIsInstance(resp, LaunchCodespaceResponse)
        self.assertEqual(resp.attached_cluster.cluster_status, "inactive")
        self.assertEqual(resp.attached_cluster.cluster_usage.memory_used, 0)
        self.assertEqual(resp.attached_cluster.cluster_usage.cores_used, 0)

    def test_creating_cluster_strategy(self):
        self.compute_cluster_details["status"] = "creating"
        resp = (
            LaunchStrategyFactory()
            .get_strategy("creating")
            .handle(
                workspace=self.mock_workspace,
                compute=self.mock_compute,
                env="darwin-local",
                codespace_details=self.codespace_details,
                project_details=self.project_details,
                user="test",
                cluster=self.compute_cluster_details,
            )
        )
        self.assertIsInstance(resp, LaunchCodespaceResponse)
        self.assertEqual(resp.attached_cluster.cluster_status, "creating")
        self.assertEqual(resp.attached_cluster.cluster_usage.memory_used, 0)
        self.assertEqual(resp.attached_cluster.cluster_usage.cores_used, 0)

    @patch("workspace_app_layer.utils.utils.requests.get")
    def test_active_cluster_strategy(self, mock_get):
        self.mock_workspace.launch_codespace_v2.return_value = {
            "jupyter_link": "https://localhost:8888/fsx/workspace/test/test/test",
            "code_server_link": "https://localhost:8888/fsx/workspace/test/test/test",
            "codespace": {},
            "project": {},
            "cluster": {},
        }
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            "data": {
                "summary": [
                    {"cpu": 0.5, "cpus": [4], "mem": [10000, 0, 0, 5000]},
                    {"cpu": 0.75, "cpus": [4], "mem": [20000, 0, 0, 15000]},
                ]
            }
        }
        self.compute_cluster_details["status"] = "active"

        resp = (
            LaunchStrategyFactory()
            .get_strategy("active")
            .handle(
                workspace=self.mock_workspace,
                compute=self.mock_compute,
                env="darwin-local",
                codespace_details=self.codespace_details,
                project_details=self.project_details,
                user="test",
                cluster=self.compute_cluster_details,
            )
        )

        self.assertIsInstance(resp, LaunchCodespaceResponse)
        self.assertEqual(resp.attached_cluster.cluster_status, "active")
        self.assertEqual(resp.jupyter_lab_link, "localhost:8888/fsx/workspace/test/test/test")
        self.assertEqual(resp.code_server_link, "localhost:8888/fsx/workspace/test/test/test")
        self.assertGreaterEqual(resp.attached_cluster.cluster_usage.memory_used, 0)
        self.assertGreaterEqual(resp.attached_cluster.cluster_usage.cores_used, 0)

    def test_exception_strategy(self):
        with self.assertRaises(Exception):
            LaunchStrategyFactory().get_strategy("invalid").handle(
                workspace=self.mock_workspace,
                compute=self.mock_compute,
                env="darwin-local",
                codespace_details=self.codespace_details,
                project_details=self.project_details,
                user="test",
                cluster=self.compute_cluster_details,
            )
