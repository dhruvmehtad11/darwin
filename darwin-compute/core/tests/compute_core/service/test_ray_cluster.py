import unittest
from unittest.mock import patch

from compute_core.service.ray_cluster import RayClusterService


class TestRayClusterService(unittest.TestCase):
    def setUp(self):
        self.ray_cluster_service = RayClusterService("http://id-test")

    @patch("requests.request")
    def test_get_summary_success(self, mock_get):
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            "data": {"summary": [{"cpus": [1], "cpu": 0.5, "mem": [1, 1, 1, 1]}]}
        }
        # Test case: Checking if the summary is fetched correctly
        result = self.ray_cluster_service.get_summary()
        self.assertIsInstance(result, list)
        self.assertIn("cpu", result[0])
        self.assertIn("mem", result[0])

    def test_get_summary_failure(self):
        with self.assertRaises(Exception):
            self.ray_cluster_service.get_summary()
