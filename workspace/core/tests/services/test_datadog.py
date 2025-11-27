import unittest

from unittest.mock import patch
from workspace_core.service.datadog import DataDog
from workspace_app_layer.models.workspace.datadog_query_request import DatadogQueryRequest


class TestDatadog(unittest.TestCase):
    def setUp(self):
        self.datadog_client = DataDog()
        self.request = DatadogQueryRequest(unix_start_time=1609459200, unix_end_time=1609545600, datadog_query="test")

    @patch("workspace_core.service.datadog._request")
    def test_fetch_datadog_metrics(self, mock_request):
        mock_request.return_value = {"test": "test"}
        request = self.request
        resp = self.datadog_client.fetch_datadog_metrics(request)
        self.assertEqual(resp, {"test": "test"})
