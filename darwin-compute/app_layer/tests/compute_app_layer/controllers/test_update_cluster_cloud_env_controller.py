import json
import unittest
from unittest.mock import patch
from compute_app_layer.controllers.cluster.update_cloud_env_controller import update_cloud_env_controller
from compute_core.compute import Compute
from compute_core.dto.exceptions import ClusterInvalidStateException


class TestUpdateCloudEnvController(unittest.IsolatedAsyncioTestCase):

    @patch("compute_core.remote_command.RemoteCommand")
    @patch("compute_core.compute")
    def setUp(self, mock_compute, mock_remote):
        self.mock_compute = mock_compute.return_value
        self.mock_remote = mock_remote.return_value
        self.compute = Compute

    async def test_update_cluster_cloud_env_controller_success(self):
        cluster_id = "test_id"
        cloud_env = "eks-0"
        self.mock_compute.update_cluster_cloud_env.return_value = None
        response = await update_cloud_env_controller(
            compute=self.mock_compute,
            remote_command=self.mock_remote,
            cluster_id=cluster_id,
            cloud_env=cloud_env,
            user="user",
        )
        body = json.loads(response.body)
        self.assertEqual(body["data"]["cluster_id"], cluster_id)
        self.assertEqual(body["data"]["cloud_env"], cloud_env)

    async def test_update_cluster_cloud_env_controller_bad_cloud_env(self):
        self.mock_compute.update_cluster_cloud_env.side_effect = ValueError("Invalid cloud environment")
        cluster_id = "test_id"
        cloud_env = "invalid-env"
        response = await update_cloud_env_controller(
            compute=self.mock_compute,
            remote_command=self.mock_remote,
            cluster_id=cluster_id,
            cloud_env=cloud_env,
            user="user",
        )
        self.assertEqual(response.status_code, 400)

    async def test_update_cluster_cloud_env_controller_cluster_state_exception(self):
        cluster_id = "test_id"
        cloud_env = "eks-0"
        self.mock_compute.update_cluster_cloud_env.side_effect = ClusterInvalidStateException(
            cluster_id, "active", "inactive"
        )

        response = await update_cloud_env_controller(
            compute=self.mock_compute,
            remote_command=self.mock_remote,
            cluster_id=cluster_id,
            cloud_env=cloud_env,
            user="user",
        )
        self.assertEqual(response.status_code, 400)

    async def test_update_cluster_cloud_env_controller_exception(self):
        self.mock_compute.update_cluster_cloud_env.side_effect = Exception("")
        cluster_id = "test_id"
        cloud_env = "eks-0"
        self.mock_compute.update_cluster_cloud_env.return_value = None
        response = await update_cloud_env_controller(
            compute=self.mock_compute,
            remote_command=self.mock_remote,
            cluster_id=cluster_id,
            cloud_env=cloud_env,
            user="user",
        )
        self.assertEqual(response.status_code, 500)
