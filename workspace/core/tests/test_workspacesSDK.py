import asyncio
import unittest
from datetime import datetime
from unittest.mock import patch

from workspace_core.dto.response import CodespaceResponse, ProjectResponse
from workspace_core.dto.response.workspace_and_codespace_response import WorkspaceAndCodespaceResponse
from workspace_core.utils.event_states import WorkspaceState
from workspace_core.workspaces import WorkspacesSDK


class TestWorkspacesSDK(unittest.TestCase):
    @patch("workspace_core.workspaces.MySQLDao")
    @patch("workspace_core.workspaces.EventAPIClient")
    def setUp(self, mock_event_client, mock_mysql_dao):
        self.sdk = WorkspacesSDK("darwin-local")
        self.mock_event_client = mock_event_client.return_value
        self.mock_mysql_dao = mock_mysql_dao.return_value
        self.codespace_not_none = CodespaceResponse(
            id=1,
            project_id=1,
            name="test",
            created_at=datetime.now(),
            updated_at=datetime.now(),
            last_synced_at=datetime.now(),
        )
        self.project = ProjectResponse(
            id=1, name="test", user_id="test", created_at="2021-09-01", updated_at="2021-09-01"
        )
        self.event_type = WorkspaceState.CREATE_CODESPACE_SUCCESSFUL

        self.workspace_and_codespace_response = WorkspaceAndCodespaceResponse(
            project_id=1, project_name="test_project", codespace_id=1, codespace_name="test_codespace"
        )

    def test_send_event_codespace_not_none(self):
        expected = None
        self.mock_event_client.create_event.return_value = None
        response = self.sdk._send_event(self.event_type, self.codespace_not_none, self.project)
        self.assertEqual(expected, response)

    def test_send_event_codespace_none(self):
        expected = None
        self.mock_event_client.create_event.return_value = None
        response = self.sdk._send_event(self.event_type, None, self.project)
        self.assertEqual(expected, response)

    def test_get_project_id_and_codespace_id_from_codespace_path(self):
        self.mock_mysql_dao.read.return_value = "test_value"
        user_id = "test"
        codespace_name = "test-codespace"
        project_name = "test-project"
        expected = "test_value"
        response = self.sdk.get_project_id_and_codespace_id_from_codespace_path(
            user_id=user_id, codespace_name=codespace_name, project_name=project_name
        )
        self.assertEqual(expected, response)

    def test_list_workspaces_and_codespaces(self):
        cluster_id = "test_id"
        self.mock_mysql_dao.read.return_value = [self.workspace_and_codespace_response.to_dict()]
        response = self.sdk.list_workspaces_and_codespaces(cluster_id)
        expected = [self.workspace_and_codespace_response]
        self.assertEqual(expected, response)
