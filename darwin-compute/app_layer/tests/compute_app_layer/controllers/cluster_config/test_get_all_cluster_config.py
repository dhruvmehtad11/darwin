import json
import unittest
from unittest.mock import patch

from compute_app_layer.controllers.cluster_config.get_all_cluster_config import get_all_cluster_config_controller
from compute_core.compute import Compute


class TestGetAllClusterConfig(unittest.IsolatedAsyncioTestCase):
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

    async def test_get_all_cluster_config_controller_success(self):
        self.mock_dao.get_all_cluster_config.return_value = [{"config_key": "test", "value": "test"}]
        res = await get_all_cluster_config_controller(self.compute, offset=0, limit=10)
        res = json.loads(res.body)
        expected = [{"key": "test", "value": "test"}]
        self.assertEqual(expected, res["data"])
        self.assertEqual(res["status"], "SUCCESS")

    async def test_get_all_cluster_config_controller_no_config_exception(self):
        self.mock_dao.get_all_cluster_config.return_value = []
        res = await get_all_cluster_config_controller(self.compute, offset=0, limit=10)
        res = json.loads(res.body)
        expected = f"Cluster configs not found"
        self.assertEqual(res["message"], expected)
        self.assertEqual(res["status"], "ERROR")

    async def test_get_all_cluster_config_controller_exception(self):
        error = "ERROR"
        self.mock_dao.get_all_cluster_config.side_effect = Exception(error)
        res = await get_all_cluster_config_controller(self.compute, offset=0, limit=10)
        res = json.loads(res.body)
        expected = f"Error in getting cluster configs: {error}"
        self.assertEqual(res["message"], expected)
        self.assertEqual(res["status"], "ERROR")
