import unittest

from pydantic import ValidationError

from compute_app_layer.models.remote_command.remote_command_request import (
    RemoteCommandRequest,
    PodCommandExecutionStatusReportRequest,
)


class TestRemoteCommandRequest(unittest.TestCase):
    def test_remote_command_request_init(self):
        request = {"command": ["ls -la"]}
        remote_command_request = RemoteCommandRequest(**request)
        self.assertEqual(remote_command_request.command, ["ls -la"])
        self.assertEqual(remote_command_request.target.value, "cluster")
        self.assertEqual(remote_command_request.status.value, "created")
        self.assertEqual(remote_command_request.timeout, 300)
        self.assertIsInstance(remote_command_request.execution_id, str)

    def test_remote_command_request_init_with_target_and_timeout(self):
        request = {"command": ["ls -la"], "target": "head", "timeout": 600}
        remote_command_request = RemoteCommandRequest(**request)
        self.assertEqual(remote_command_request.command, ["ls -la"])
        self.assertEqual(remote_command_request.target.value, "head")
        self.assertEqual(remote_command_request.status.value, "created")
        self.assertEqual(remote_command_request.timeout, 600)
        self.assertIsInstance(remote_command_request.execution_id, str)

    def test_remote_command_request_init_with_execution_id(self):
        request = {"command": ["ls -la"], "execution_id": "1234"}
        remote_command_request = RemoteCommandRequest(**request)
        self.assertEqual(remote_command_request.command, ["ls -la"])
        self.assertEqual(remote_command_request.target.value, "cluster")
        self.assertEqual(remote_command_request.status.value, "created")
        self.assertEqual(remote_command_request.timeout, 300)
        self.assertEqual(remote_command_request.execution_id, "1234")

    def test_remote_command_request_convert(self):
        request = {"command": ["ls -la", "echo Hello"], "target": "head", "timeout": 600}
        remote_command_request = RemoteCommandRequest(**request)
        converted_request = remote_command_request.convert()
        self.assertEqual(converted_request.command, "ls -la ; echo Hello")
        self.assertEqual(converted_request.target.value, "head")
        self.assertEqual(converted_request.timeout, 600)
        self.assertIsInstance(converted_request.execution_id, str)

    def test_pod_command_execution_status_report_request_init(self):
        request = {"cluster_id": "cluster_id", "execution_id": "1234", "pod_name": "pod_name", "status": "success"}
        pod_command_execution_status_report_request = PodCommandExecutionStatusReportRequest(**request)
        self.assertEqual(pod_command_execution_status_report_request.cluster_id, "cluster_id")
        self.assertEqual(pod_command_execution_status_report_request.execution_id, "1234")
        self.assertEqual(pod_command_execution_status_report_request.pod_name, "pod_name")
        self.assertEqual(pod_command_execution_status_report_request.status.value, "success")

    def test_pod_command_execution_status_report_request_init_with_any_field_missing(self):
        # Missing status
        request = {"cluster_id": "cluster_id", "execution_id": "1234", "pod_name": "pod_name"}
        with self.assertRaises(ValidationError):
            PodCommandExecutionStatusReportRequest(**request)

        # Missing pod_name
        request = {"cluster_id": "cluster_id", "execution_id": "1234", "status": "success"}
        with self.assertRaises(ValidationError):
            PodCommandExecutionStatusReportRequest(**request)
