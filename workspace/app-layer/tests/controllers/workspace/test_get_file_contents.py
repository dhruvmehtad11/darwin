import asyncio
import json
import unittest
from unittest.mock import patch

from workspace_app_layer.controllers.workspace.get_file_contents import get_file_contents_controller
from workspace_app_layer.exceptions.exceptions import BadRequestException
from workspace_app_layer.models.workspace.get_file_contents_request import GetFileContents


class TestGetFileContents(unittest.TestCase):
    @patch("workspace_core.workspaces.WorkspacesSDK")
    @patch("workspace_core.service.compute.Compute")
    def setUp(self, mock_compute, mock_workspace):
        self.mock_compute = mock_compute.return_value
        self.mock_workspace = mock_workspace.return_value
        self.request = GetFileContents(source_path="valid.json")
        self.mock_workspace.get_file_contents.return_value = {"key": "value"}

    async def test_get_file_contents_controller_success(self):
        resp = await get_file_contents_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "SUCCESS")
        self.assertEqual(resp["message"], "File contents retrieved successfully from valid.json")
        self.assertEqual(resp["data"], {"key": "value"})

    async def test_file_not_found_exception(self):
        self.mock_workspace.get_file_contents.side_effect = FileNotFoundError()
        resp = await get_file_contents_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "Source path valid.json does not exist in EFS")
        self.assertIsNone(resp["data"])

    async def test_file_size_exceeded_exception(self):
        self.mock_workspace.get_file_contents.side_effect = BadRequestException("File size is greater than 2MB")
        resp = await get_file_contents_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "File size is greater than 2MB")
        self.assertIsNone(resp["data"])

    async def test_invalid_file_type_exception(self):
        self.mock_workspace.get_file_contents.side_effect = BadRequestException("File type not supported")
        resp = await get_file_contents_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "File type not supported")
        self.assertIsNone(resp["data"])

    async def test_invalid_json_content_exception(self):
        self.mock_workspace.get_file_contents.side_effect = BadRequestException(
            "Invalid JSON content in file 'valid.json'"
        )
        resp = await get_file_contents_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "Invalid JSON content in file 'valid.json'")
        self.assertIsNone(resp["data"])

    async def test_generic_exception(self):
        self.mock_workspace.get_file_contents.side_effect = Exception("Unexpected error")
        resp = await get_file_contents_controller(workspace=self.mock_workspace, request=self.request)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
        self.assertEqual(resp["message"], "Unexpected error")
        self.assertIsNone(resp["data"])

    def run_async_test(self, coro):
        return asyncio.get_event_loop().run_until_complete(coro)

    def test_all(self):
        self.run_async_test(self.test_get_file_contents_controller_success())
        self.run_async_test(self.test_file_not_found_exception())
        self.run_async_test(self.test_file_size_exceeded_exception())
        self.run_async_test(self.test_invalid_file_type_exception())
        self.run_async_test(self.test_invalid_json_content_exception())
        self.run_async_test(self.test_generic_exception())
