import json
import unittest
from unittest.mock import patch

from workspace_app_layer.controllers.workspace.get_project_id_and_codespace_id_rom_codespace_path import (
    get_project_id_and_codespace_id_from_codespace_path,
)
from workspace_app_layer.models.workspace.codespace_path_request import CodespacePathRequest


class TestGetProjectIdAndCodespaceIdFromCodespacePath(unittest.TestCase):
    @patch("workspace_core.workspaces.WorkspacesSDK")
    @patch("workspace_core.service.compute.Compute")
    def setUp(self, mock_compute, mock_workspace):
        self.mock_compute = mock_compute.return_value
        self.mock_workspace = mock_workspace.return_value

    def test_get_project_id_and_codespace_id_from_codespace_path_success(self):
        request = CodespacePathRequest(codespace_path="user/project/codespace/file")
        self.mock_workspace.get_project_id_and_codespace_id_from_codespace_path.return_value = [
            {"project_id": "test", "codespace_id": "test"}
        ]
        resp = get_project_id_and_codespace_id_from_codespace_path(workspace=self.mock_workspace, request=request)
        expected = [{"project_id": "test", "codespace_id": "test"}]
        print(resp)
        self.assertEqual(expected, resp["data"])
        self.assertEqual("SUCCESS", resp["status"])

    def test_get_project_id_and_codespace_id_from_codespace_path_failure(self):
        request = CodespacePathRequest(codespace_path="user/project/codespace/file")
        error_message = "ERROR"
        self.mock_workspace.get_project_id_and_codespace_id_from_codespace_path.side_effect = Exception(error_message)
        resp = get_project_id_and_codespace_id_from_codespace_path(workspace=self.mock_workspace, request=request)
        resp = json.loads(resp.body)
        print("resp = ", resp)
        self.assertEqual("ERROR", resp["status"])
