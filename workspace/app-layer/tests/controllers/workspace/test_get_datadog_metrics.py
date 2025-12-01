import asyncio
import json
import unittest

from unittest.mock import patch
from workspace_app_layer.controllers.workspace.get_datadog_metrics import datadog_metrics_controller
from workspace_app_layer.models.workspace.datadog_query_request import DatadogQueryRequest


class TestDatadogMetricsController(unittest.TestCase):
    @patch("workspace_core.service.datadog.DataDog")
    def setUp(self, mock_datadog_client):
        self.mock_datadog_client = mock_datadog_client.return_value
        self.mock_datadog_response = "test"
        self.mock_datadog_error_response = {"message": "error", "status": "ERROR"}
        self.request = DatadogQueryRequest(
            unix_start_time=1739198599,
            unix_end_time=1739284999,
            datadog_query="p95:trace.fastapi.request{env:prod,service:darwin-workspace ,resource_name:post_/launch-codespace/v2}",
        )

    async def test_datadog_metrics_success(self):
        self.mock_datadog_client.fetch_datadog_metrics.return_value = self.mock_datadog_response
        response = await datadog_metrics_controller(self.mock_datadog_client, request=self.request)
        response = json.loads(response.body)
        expected_response = {
            "data": "test",
            "message": f"Successfully fetched datadog metrics with query: {self.request.datadog_query}",
            "status": "SUCCESS",
        }
        self.assertEqual(expected_response, response)

    async def test_datadog_metrics_failure(self):
        request = self.request
        self.mock_datadog_client.fetch_datadog_metrics.side_effect = Exception("error")
        response = await datadog_metrics_controller(self.mock_datadog_client, request)
        response = json.loads(response.body)

        self.assertEqual(self.mock_datadog_error_response, response)

    def run_async_test(self, coro):
        return asyncio.get_event_loop().run_until_complete(coro)

    def test_all(self):
        self.run_async_test(self.test_datadog_metrics_success())
        self.run_async_test(self.test_datadog_metrics_failure())
