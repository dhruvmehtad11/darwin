import json
import unittest
from unittest.mock import patch

from fastapi.responses import JSONResponse

from compute_app_layer.controllers.remote_command.remote_command_execution_controller import (
    add_remote_commands_to_cluster_controller,
    execute_command_on_cluster_controller,
    delete_remote_command_from_cluster_controller,
    get_command_execution_status_controller,
    set_pod_command_execution_status_controller,
    get_cluster_remote_commands_controller,
)
from compute_app_layer.models.remote_command.remote_command_request import (
    AddRemoteCommandsRequest,
    RemoteCommandRequest,
    PodCommandExecutionStatusReportRequest,
)
from compute_core.dto.remote_command_dto import RemoteCommandTarget, RemoteCommandStatus


class TestRemoteCommandController(unittest.IsolatedAsyncioTestCase):
    @patch("compute_core.remote_command.RemoteCommand")
    def setUp(self, mock_rc):
        self.mock_rc = mock_rc.return_value

    async def test_add_remote_commands_to_cluster_controller(self):
        request = AddRemoteCommandsRequest(
            remote_commands=[
                RemoteCommandRequest(command=["test_command_1"], target=RemoteCommandTarget.cluster),
                RemoteCommandRequest(command=["test_command_2"], target=RemoteCommandTarget.head),
            ]
        )
        expected_response_body = {
            "remote_commands": [{"execution_id": "test_1"}, {"execution_id": "test_2"}],
            "artifact_name": "test_artifact_name",
        }
        self.mock_rc.add_to_cluster.return_value = expected_response_body

        response = await add_remote_commands_to_cluster_controller("cluster_id", request, self.mock_rc)
        self.assertIsInstance(response, JSONResponse)
        self.assertEqual(response.status_code, 200)

        response = json.loads(response.body)

        assert response["status"] == "SUCCESS"
        assert response["data"] == expected_response_body

    async def test_execute_command_on_cluster_controller(self):
        request = RemoteCommandRequest(command=["test_command_1"], target=RemoteCommandTarget.cluster)
        expected_response_body = {"execution_id": "test_1"}
        self.mock_rc.execute_on_cluster.return_value = expected_response_body

        response = await execute_command_on_cluster_controller("cluster_id", request, self.mock_rc)
        self.assertIsInstance(response, JSONResponse)
        self.assertEqual(response.status_code, 202)

        response = json.loads(response.body)
        assert response["status"] == "SUCCESS"
        assert response["data"] == expected_response_body

    async def test_delete_remote_command_from_cluster_controller(self):
        expected_response_body = {"execution_ids": ["test_1"]}
        self.mock_rc.delete_from_cluster.return_value = expected_response_body

        response = await delete_remote_command_from_cluster_controller("cluster_id", "execution_id", self.mock_rc)
        self.assertIsInstance(response, JSONResponse)
        self.assertEqual(response.status_code, 200)

        response = json.loads(response.body)
        assert response["status"] == "SUCCESS"
        assert response["data"] == {"execution_id": "test_1"}

    async def test_get_command_execution_status_controller(self):
        expected_response_body = {"status": "created", "execution_id": "test_1"}
        self.mock_rc.get_execution_status.return_value = expected_response_body

        response = await get_command_execution_status_controller("cluster_id", "test_1", self.mock_rc)
        self.assertIsInstance(response, JSONResponse)
        self.assertEqual(response.status_code, 200)

        response = json.loads(response.body)
        assert response["status"] == "SUCCESS"
        assert response["data"] == expected_response_body

    async def test_get_cluster_remote_commands_controller(self):
        expected_response_body = {
            "remote_commands": [
                {"execution_id": "test_1", "status": "running"},
                {"execution_id": "test_2", "status": "success"},
            ]
        }
        self.mock_rc.get_all_of_cluster.return_value = expected_response_body["remote_commands"]

        response = await get_cluster_remote_commands_controller("cluster_id", self.mock_rc)
        self.assertIsInstance(response, JSONResponse)
        self.assertEqual(response.status_code, 200)

        response = json.loads(response.body)
        assert response["status"] == "SUCCESS"
        assert response["data"] == expected_response_body

    async def test_set_pod_command_execution_status_controller(self):
        request = PodCommandExecutionStatusReportRequest(
            cluster_id="test_cluster_id",
            execution_id="test_execution_id",
            pod_name="test_pod_name",
            status=RemoteCommandStatus.SUCCESS,
        )
        expected_response_body = {
            "status": "success",
            "execution_id": "test_1",
            "pod_name": "test_pod_name",
            "cluster_run_id": "test_cluster_run_id",
        }
        self.mock_rc.set_pod_status.return_value = expected_response_body

        response = await set_pod_command_execution_status_controller(request, self.mock_rc)
        self.assertIsInstance(response, JSONResponse)
        self.assertEqual(response.status_code, 200)

        response = json.loads(response.body)
        assert response["status"] == "SUCCESS"
        assert response["data"] == expected_response_body
