import pytest
import unittest
from unittest.mock import patch

from workspace_app_layer.models.workspace.response.launch_codespace_response import AttachedCluster
from workspace_app_layer.utils.response_mapper import attached_cluster_details


class TestResponseMapper(unittest.TestCase):
    @patch("workspace_core.workspaces.WorkspacesSDK")
    @patch("workspace_core.service.compute.Compute")
    def setUp(self, mock_compute_service, mock_workspace):
        self.mock_compute_service = mock_compute_service.return_value
        self.mock_workspace = mock_workspace.return_value
        self.mock_compute_service.get_cluster_dashboards.return_value = self.compute_cluster_details["dashboards"][
            "data"
        ]
        self.mock_workspace.attached_codespaces_count.return_value = 1

    @pytest.fixture(autouse=True)
    def prepare_fixture(self, compute_cluster_details):
        self.compute_cluster_details = compute_cluster_details

    def test_attached_cluster_details_inactive(self):
        attached_cluster = attached_cluster_details(
            self.compute_cluster_details, self.mock_compute_service, self.mock_workspace
        )
        self.assertIsInstance(attached_cluster, AttachedCluster)
        self.assertEqual(attached_cluster.cluster_usage.memory_used, 0)
        self.assertEqual(attached_cluster.cluster_usage.cores_used, 0)
        self.assertEqual(attached_cluster.cluster_status, "inactive")

    @patch("workspace_app_layer.utils.utils._request")
    def test_attached_cluster_details_active(self, mock_get):
        mock_get.return_value = {
            "data": {
                "summary": [
                    {"cpu": 0.5, "cpus": [4], "mem": [10000, 0, 0, 5000]},
                    {"cpu": 0.75, "cpus": [4], "mem": [20000, 0, 0, 15000]},
                ]
            }
        }
        self.compute_cluster_details["status"] = "active"

        attached_cluster = attached_cluster_details(
            self.compute_cluster_details, self.mock_compute_service, self.mock_workspace
        )

        self.assertIsInstance(attached_cluster, AttachedCluster)
        self.assertEqual(attached_cluster.cluster_usage.memory_used, 67)
        self.assertEqual(attached_cluster.cluster_usage.cores_used, 1)
        self.assertEqual(attached_cluster.cluster_status, "active")
