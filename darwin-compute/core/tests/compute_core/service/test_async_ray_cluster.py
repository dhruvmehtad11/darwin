import unittest
from unittest.mock import patch, AsyncMock
import asyncio

from compute_core.service.ray_cluster import AsyncRayClusterService


class TestAsyncRayClusterService(unittest.IsolatedAsyncioTestCase):

    def setUp(self):
        """Set up test fixtures"""
        self.service = AsyncRayClusterService("http://test-dashboard:8265")
        self.mock_response = {
            "data": {
                "summary": [
                    {"cpus": [4], "cpu": 0.5, "mem": [16000, 16000, 16000, 8000]},
                    {"cpus": [8], "cpu": 0.75, "mem": [32000, 32000, 32000, 24000]},
                ]
            }
        }

    @patch("compute_core.service.ray_cluster.make_async_api_request")
    async def test_get_summary_success(self, mock_api_request):
        """Test successful async get_summary call"""
        mock_api_request.return_value = self.mock_response

        result = await self.service.get_summary()

        expected_url = "http://test-dashboard:8265nodes?view=summary"
        mock_api_request.assert_called_once_with(method="GET", url=expected_url, timeout=2, max_retries=3)

        self.assertEqual(result, self.mock_response["data"]["summary"])
        self.assertIsInstance(result, list)
        self.assertEqual(len(result), 2)

    @patch("compute_core.service.ray_cluster.make_async_api_request")
    async def test_get_summary_with_trailing_slash(self, mock_api_request):
        """Test get_summary with dashboard URL that has trailing slash"""
        service_with_slash = AsyncRayClusterService("http://test-dashboard:8265/")
        mock_api_request.return_value = self.mock_response

        await service_with_slash.get_summary()

        expected_url = "http://test-dashboard:8265/nodes?view=summary"
        mock_api_request.assert_called_once_with(method="GET", url=expected_url, timeout=2, max_retries=3)

    @patch("compute_core.service.ray_cluster.make_async_api_request")
    async def test_get_summary_api_error(self, mock_api_request):
        """Test get_summary when API request fails"""
        mock_api_request.side_effect = Exception("Connection timeout")

        with self.assertRaises(Exception) as context:
            await self.service.get_summary()

        self.assertIn("Connection timeout", str(context.exception))

    @patch("compute_core.service.ray_cluster.make_async_api_request")
    async def test_get_summary_empty_response(self, mock_api_request):
        """Test get_summary with empty node summary"""
        empty_response = {"data": {"summary": []}}
        mock_api_request.return_value = empty_response

        result = await self.service.get_summary()

        self.assertEqual(result, [])

    @patch("compute_core.service.ray_cluster.make_async_api_request")
    async def test_get_summary_malformed_response(self, mock_api_request):
        """Test get_summary with malformed response"""
        malformed_response = {"data": {}}  # Missing 'summary' key
        mock_api_request.return_value = malformed_response

        with self.assertRaises(KeyError):
            await self.service.get_summary()

    async def test_concurrent_requests(self):
        """Test that multiple concurrent requests work correctly"""
        services = [AsyncRayClusterService(f"http://test-dashboard-{i}:8265") for i in range(5)]

        with patch("compute_core.service.ray_cluster.make_async_api_request") as mock_api_request:
            mock_api_request.return_value = self.mock_response

            # Execute concurrent requests
            tasks = [service.get_summary() for service in services]
            results = await asyncio.gather(*tasks)

            # Verify all requests completed successfully
            self.assertEqual(len(results), 5)
            for result in results:
                self.assertEqual(result, self.mock_response["data"]["summary"])

            # Verify correct number of API calls
            self.assertEqual(mock_api_request.call_count, 5)
