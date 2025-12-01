import asyncio
import json
import unittest
from unittest.mock import patch

from workspace_app_layer.controllers.workspace.upload_to_s3 import upload_to_s3_controller
from workspace_app_layer.exceptions.exceptions import BadRequestException
from workspace_app_layer.models.workspace.upload_to_s3_request import UploadToS3Request


class TestUploadToS3(unittest.TestCase):
    @patch("workspace_core.workspaces.WorkspacesSDK")
    @patch("workspace_core.service.compute.Compute")
    def setUp(self, mock_compute, mock_workspace):
        self.mock_compute = mock_compute.return_value
        self.mock_workspace = mock_workspace.return_value
        self.request = UploadToS3Request(source_path="test", s3_bucket="test", destination_path="test")
        self.mock_workspace.upload_to_s3.return_value = None

    async def test_upload_to_s3_controller_success(self):
        resp = await upload_to_s3_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "SUCCESS")
        self.assertEqual(resp["message"], "Uploaded test to S3")

    async def test_file_not_found_exception(self):
        self.mock_workspace.upload_to_s3.side_effect = FileNotFoundError("")
        resp = await upload_to_s3_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)

        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "Source path test does not exist in EFS")

    async def test_bad_request_exception(self):
        self.mock_workspace.upload_to_s3.side_effect = BadRequestException(f"Size > 10mb")
        resp = await upload_to_s3_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)

        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "Size > 10mb")

    async def test_upload_to_s3_controller_exception(self):
        self.mock_workspace.upload_to_s3.side_effect = Exception("error")
        resp = await upload_to_s3_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)

        self.assertEqual(resp["status"], "ERROR")

    async def test_access_denied_exception(self):
        self.mock_workspace.upload_to_s3.side_effect = Exception("Access Denied")
        resp = await upload_to_s3_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)

        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "S3 bucket test is not accessible")

    def run_async_test(self, coro):
        return asyncio.get_event_loop().run_until_complete(coro)

    def test_all(self):
        self.run_async_test(self.test_upload_to_s3_controller_success())
        self.run_async_test(self.test_file_not_found_exception())
        self.run_async_test(self.test_bad_request_exception())
        self.run_async_test(self.test_upload_to_s3_controller_exception())
        self.run_async_test(self.test_access_denied_exception())
