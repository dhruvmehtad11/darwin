import json
import unittest
from unittest.mock import patch

from compute_app_layer.controllers.get_job_clusters_used_before_days import get_job_clusters_used_before_days_controller
from compute_core.compute import Compute


class TestJobClusterUsedBeforeDays(unittest.IsolatedAsyncioTestCase):
    @patch("compute_core.compute.ClusterDao")
    @patch("compute_core.compute.DarwinClusterManager")
    @patch("compute_core.compute.EventService")
    @patch("compute_core.compute.RuntimeDao")
    def setUp(self, mock_runtime_dao, mock_event_service, mock_dcm, mock_dao):
        self.compute = Compute("test")
        self.mock_dcm = mock_dcm.return_value
        self.mock_dao = mock_dao.return_value
        self.mock_runtime_dao = mock_runtime_dao.return_value
        self.mock_event_service = mock_event_service.return_value

    async def test_get_job_cluster_controller_success(self):
        self.mock_dao.get_job_cluster_ids.return_value = ["cluster_id"]
        self.mock_dao.get_clusters_last_used_before_days.return_value = [{"cluster_id": "test_id"}]
        res = await get_job_clusters_used_before_days_controller(self.compute, offset=1, limit=1, days=1)
        res = json.loads(res.body)
        expected = [{"cluster_id": "test_id"}]
        self.assertEqual(res["data"]["cluster_ids"], expected)
        self.assertEqual(res["status"], "SUCCESS")

    async def test_get_job_cluster_controller_es_result_exception(self):
        error = "ERROR"
        self.mock_dao.get_job_cluster_ids.side_effect = Exception(error)
        res = await get_job_clusters_used_before_days_controller(self.compute, offset=1, limit=1, days=1)
        res = json.loads(res.body)
        expected = f"Error in getting job clusters: {error}"
        self.assertEqual(res["message"], expected)
        self.assertEqual(res["status"], "ERROR")

    async def test_get_job_cluster_controller_sql_result_exception(self):
        error = "ERROR"
        self.mock_dao.get_clusters_last_used_before_days.side_effect = Exception(error)
        res = await get_job_clusters_used_before_days_controller(self.compute, offset=1, limit=1, days=1)
        res = json.loads(res.body)
        expected = f"Error in getting job clusters: {error}"
        self.assertEqual(res["message"], expected)
        self.assertEqual(res["status"], "ERROR")
