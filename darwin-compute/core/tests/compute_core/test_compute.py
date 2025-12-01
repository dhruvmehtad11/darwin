import unittest
from datetime import datetime
from unittest.mock import patch, Mock

import pytest

from compute_core.compute import Compute
from compute_core.util.utils import generate_ray_cluster_dashboard_url


class TestCompute(unittest.TestCase):
    @pytest.fixture(autouse=True)
    def prepare_fixture(self, es_compute_def):
        self.es_compute_def = es_compute_def

    @patch("compute_core.compute.ClusterDao")
    @patch("compute_core.compute.DarwinClusterManager")
    @patch("compute_core.compute.EventService")
    @patch("compute_core.compute.RuntimeDao")
    def setUp(self, mock_runtime_dao, mock_event_service, mock_dcm, mock_cluster_dao):
        self.mock_runtime_dao = mock_runtime_dao.return_value
        self.mock_event_service = mock_event_service.return_value
        self.mock_dcm = mock_dcm.return_value
        self.mock_cluster_dao = mock_cluster_dao.return_value
        self.compute = Compute("test")

    @patch("compute_core.compute.RayClusterService")
    def test_get_active_resources(self, mock_ray_cluster_service):
        self.mock_cluster_dao.get_cluster_info.return_value = self.es_compute_def
        mock_ray_cluster_service.return_value.get_summary.return_value = [
            {"cpus": [1], "cpu": 0.5, "mem": [1, 1, 1, 1]}
        ]

        resources = self.compute.get_active_resources("test_id")
        self.assertEqual(resources.cores_used, 0)
        self.assertEqual(resources.memory_used, 100)

    def test_get_active_resources_exception(self):
        self.mock_cluster_dao.get_cluster_info.return_value = self.es_compute_def
        resources = self.compute.get_active_resources("test_id")
        self.assertEqual(resources.cores_used, 0)
        self.assertEqual(resources.memory_used, 0)

    @patch("compute_core.util.utils.urljoin")
    @patch("compute_core.util.utils.datetime")
    def test_generate_ray_cluster_dashboard_url(self, mock_datetime, mock_urljoin):
        mock_datetime.now.return_value = datetime(2023, 1, 1)
        mock_datetime.fromisoformat.return_value = datetime(2023, 1, 1)
        mock_dao = Mock()
        mock_dao.get_cluster_last_started_at.return_value = None
        mock_dao.get_cluster_last_stopped_at.return_value = None
        cluster_details = Mock()
        cluster_details.created_on = "2023-01-01T00:00:00"

        expected_url = "https://datadog.example.com/dashboard/dashboard-123?fromUser=false&refresh_mode=paused&tpl_var_darwin_cluster_id%5B0%5D=test-cluster&from_ts=1672531200000&to_ts=1672617600000&live=false"
        mock_urljoin.return_value = expected_url

        generated_url = generate_ray_cluster_dashboard_url(
            "test-cluster", "https://datadog.example.com", "dashboard-123", mock_dao
        )
        self.assertEqual(generated_url, expected_url)

    @patch("compute_core.util.utils.urljoin")
    @patch("compute_core.util.utils.datetime")
    def test_generate_ray_cluster_dashboard_url_with_times(self, mock_datetime, mock_urljoin):
        mock_datetime.now.return_value = datetime(2023, 1, 1)
        mock_datetime.fromisoformat.return_value = datetime(2023, 1, 1)
        mock_dao = Mock()
        mock_dao.get_cluster_last_started_at.return_value = datetime(2023, 1, 1)
        mock_dao.get_cluster_last_stopped_at.return_value = datetime(2023, 1, 2)
        cluster_details = Mock()
        cluster_details.created_on = "2023-01-01T00:00:00"

        expected_url = "https://datadog.example.com/dashboard/dashboard-123?fromUser=false&refresh_mode=paused&tpl_var_darwin_cluster_id%5B0%5D=test-cluster&from_ts=1672531200000&to_ts=1672617600000&live=false"
        mock_urljoin.return_value = expected_url

        generated_url = generate_ray_cluster_dashboard_url(
            "test-cluster", "https://datadog.example.com", "dashboard-123", mock_dao
        )
        self.assertEqual(generated_url, expected_url)
