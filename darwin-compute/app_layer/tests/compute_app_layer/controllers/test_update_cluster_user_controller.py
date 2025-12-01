import json
import unittest
from unittest.mock import patch
from compute_app_layer.controllers.cluster.update_cluster_user import update_cluster_user_controller
from compute_core.compute import Compute


class TestUpdateClusterUser(unittest.IsolatedAsyncioTestCase):
    @patch("compute_core.compute")
    def setUp(self, mock_compute):
        self.mock_compute = mock_compute.return_value
        self.compute = Compute

    async def test_update_cluster_user_controller_success(self):
        cluster_id = "test_id"
        cluster_user = "test_user"
        old_test_user = "old_test_user"
        self.mock_compute.update_cluster_user.return_value = cluster_user
        user = await update_cluster_user_controller(
            compute=self.mock_compute,
            cluster_id=cluster_id,
            new_user=cluster_user,
            user=old_test_user,
        )
        user = json.loads(user.body)
        self.assertEqual(user["data"], cluster_user)

    async def test_update_cluster_user_controller_exception(self):
        self.mock_compute.update_cluster_user.side_effect = Exception("")
        cluster_id = "test_id"
        cluster_user = "test_user"
        old_test_user = "old_test_user"
        self.mock_compute.update_cluster_user.return_value = cluster_user
        user = await update_cluster_user_controller(
            compute=self.mock_compute,
            cluster_id=cluster_id,
            new_user=cluster_user,
            user=old_test_user,
        )
        self.assertEqual(user.status_code, 500)
