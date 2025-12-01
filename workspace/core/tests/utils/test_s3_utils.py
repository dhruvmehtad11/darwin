import os
from unittest import TestCase
from unittest.mock import patch

from workspace_app_layer.exceptions.exceptions import BadRequestException
from workspace_core.utils.s3_utils import S3Utils, is_file_type_valid, is_file_size_valid


class TestS3Utils(TestCase):
    @patch("workspace_core.utils.s3_utils.boto3.client")
    def setUp(self, mock_boto3_client):
        self.boto3_client = mock_boto3_client.return_value
        self.s3_utils = S3Utils()

    @patch("os.path.getsize")
    def test_upload_file(self, mock_getsize):
        self.boto3_client.upload_file.return_value = None
        mock_getsize.return_value = 500

        resp = self.s3_utils.upload_file(os.path.join(os.getcwd(), "utils/__init__.py"), "test-bucket", "test-key")

        self.assertIsNone(resp)

    @patch("os.path.getsize")
    def test_invalid_file_size(self, mock_getsize):
        mock_getsize.return_value = 10 * 1024 * 1024 + 1

        with self.assertRaises(BadRequestException) as e:
            is_file_size_valid("test.txt", 10)

    def test_invalid_file_type(self):
        with self.assertRaises(BadRequestException) as e:
            is_file_type_valid("test.pickl", (".txt, .json, .csv"))
