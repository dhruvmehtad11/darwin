from unittest import TestCase
from unittest.mock import patch

import pytest

from workspace_app_layer.models.workspace.response.launch_codespace_response import CodespacePathDetailsResponse
from workspace_app_layer.utils.utils import get_active_resources, get_info_from_codespace_path


class TestUtils(TestCase):
    @patch("workspace_core.service.compute.Compute")
    def setUp(self, mock_compute):
        self.mock_compute = mock_compute.return_value
        self.mock_compute.get_cluster_dashboards.return_value = self.get_cluster_dashboards

    @pytest.fixture(autouse=True)
    def prepare_fixture(self, get_cluster_dashboards):
        self.get_cluster_dashboards = get_cluster_dashboards

    @patch("workspace_app_layer.utils.utils._request")
    def test_get_active_resources_success(self, mock_get):
        mock_get.return_value = {
            "data": {
                "summary": [
                    {"cpus": [4], "cpu": 0.5, "mem": [16000, 0, 0, 8000]},
                    {"cpus": [4], "cpu": 0.75, "mem": [16000, 0, 0, 12000]},
                ]
            }
        }

        resp = get_active_resources(self.mock_compute, "abc")
        self.assertEqual(resp["cores_used"], 1)
        self.assertEqual(resp["memory_used"], 62)

    @patch("workspace_app_layer.utils.utils._request")
    def test_get_active_resources_failure(self, mock_get):
        resp = get_active_resources(self.mock_compute, "abc")
        self.assertEqual(resp["cores_used"], 0)
        self.assertEqual(resp["memory_used"], 0)

    @patch("workspace_app_layer.utils.utils._request")
    def test_get_active_resource_divide_by_zero_error(self, mock_get):
        mock_get.return_value = {"data": {"summary": []}}

        resp = get_active_resources(self.mock_compute, "abc")
        self.assertEqual(resp["cores_used"], 0)
        self.assertEqual(resp["memory_used"], 0)

    def test_get_info_from_codespace_path(self):
        codespace_path = "user/project/codespace"
        expected = CodespacePathDetailsResponse(user_id="user", project_name="project", codespace_name="codespace")
        resp = get_info_from_codespace_path(codespace_path=codespace_path)

        self.assertEqual(expected, resp)
