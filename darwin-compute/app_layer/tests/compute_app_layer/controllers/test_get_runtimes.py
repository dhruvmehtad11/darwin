import json
import unittest
from unittest.mock import patch

from compute_app_layer.controllers import get_runtimes_controller


class TestGetRuntimes(unittest.IsolatedAsyncioTestCase):
    @patch("compute_core.compute")
    def setUp(self, mock_compute):
        self.mock_compute = mock_compute.return_value
        self.mock_compute.get_runtimes.return_value = [
            {"id": 180, "runtime": "cpu1", "created_by": "darwin", "type": "cpu", "image": "cpu1-image"},
            {"id": 181, "runtime": "gpu1", "created_by": "darwin", "type": "gpu", "image": "gpu1-image"},
            {"id": 182, "runtime": "custom1", "created_by": "darwin", "type": "custom", "image": "custom1-image"},
            {"id": 183, "runtime": "other1", "created_by": "darwin", "type": "others", "image": "other1-image"},
            {"id": 184, "runtime": "cpu2", "created_by": "darwin", "type": "cpu", "image": "cpu2-image"},
            {"id": 185, "runtime": "other2", "created_by": "darwin", "type": "others", "image": "other2-image"},
        ]

    async def test_get_runtimes_controller_success(self):
        resp = await get_runtimes_controller(self.mock_compute)
        resp_body = json.loads(resp.body)

        self.assertEqual(resp.status_code, 200)
        self.assertEqual(len(resp_body["data"]["cpu"]), 2)
        self.assertEqual(len(resp_body["data"]["gpu"]), 1)
        self.assertEqual(len(resp_body["data"]["custom"]), 1)
        self.assertEqual(len(resp_body["data"]["others"]), 2)

    async def test_get_runtimes_controller_exception(self):
        self.mock_compute.get_runtimes.side_effect = Exception("Error while fetching runtimes")
        resp = await get_runtimes_controller(self.mock_compute)
        self.assertEqual(resp.status_code, 500)
