import unittest

from compute_model.advance_config import AdvanceConfig
from compute_model.compute_cluster import ComputeClusterDefinition
from compute_model.head_node import HeadNode
from compute_model.ray_start_params import RayStartParams
from compute_model.worker_group import WorkerGroup


class TestComputeClusterDefinition(unittest.TestCase):
    def setUp(self):
        self.name = "test-cluster"
        self.tags = ["test"]
        self.labels = {"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"}
        self.runtime = "d11-mlp-runtime-ray-2.2.0"
        self.head_node = HeadNode({"cores": 2, "memory": 4})
        self.worker_group = [WorkerGroup({"cores": 2, "memory": 4}, 1, 2)]
        self.inactive_time = 30
        self.user = "darwin@dream11.com"

    def test_valid_object_creation(self):
        # Test case: Check if object creation is successful with valid types and values
        cluster = ComputeClusterDefinition(
            name=self.name,
            tags=self.tags,
            labels=self.labels,
            runtime=self.runtime,
            head_node=self.head_node,
            worker_group=self.worker_group,
            terminate_after_minutes=self.inactive_time,
        )
        self.assertEqual(cluster.name, self.name)
        self.assertEqual(cluster.tags, self.tags)
        self.assertEqual(cluster.runtime, self.runtime)
        self.assertEqual(cluster.head_node, self.head_node)
        self.assertEqual(cluster.worker_group, self.worker_group)
        self.assertEqual(cluster.terminate_after_minutes, self.inactive_time)

    def test_invalid_object_types(self):
        # Test case: Check if object creation fails with invalid name
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=1,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
            )

        # Test case: Check if object creation fails with invalid tags
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=self.name,
                tags="test",
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
            )

        # Test case: Check if object creation fails with invalid runtime
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=1,
                head_node=self.head_node,
                worker_group=self.worker_group,
            )

        # Test case: Check if object creation fails with invalid head_node
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=1,
                worker_group=self.worker_group,
            )

        # Test case: Check if object creation fails with invalid worker_group
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=1,
            )

        # Test case: Check if object creation fails with invalid terminate_after_minutes
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                terminate_after_minutes="2",
            )

        # Test case: Check if object creation fails with invalid auto_termination_policies
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                auto_termination_policies="test",
            )

        # Test case: Check if object creation fails with invalid advance_config
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                advance_config="test",
            )

        # Test case: Check if object creation fails with invalid user
        with self.assertRaises(TypeError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                user=1,
            )

    def test_invalid_object_values(self):
        # Test case: Check if object creation fails with invalid terminate_after_minutes
        with self.assertRaises(ValueError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                terminate_after_minutes=2,  # less than 5 minutes
            )

    def test_cluster_name(self):
        # Test Case: Check if object creation passes with valid cluster name
        cluster = ComputeClusterDefinition(
            name="1est-cl_s1er",
            tags=self.tags,
            labels=self.labels,
            runtime=self.runtime,
            head_node=self.head_node,
            worker_group=self.worker_group,
        )
        self.assertEqual(cluster.name, "1est-cl_s1er")

        # Test case: Check if object creation fails with space in cluster name
        with self.assertRaises(ValueError):
            ComputeClusterDefinition(
                name="test cluster",
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
            )

        # Test case: Check if object creation fails with special character in cluster name
        with self.assertRaises(ValueError):
            ComputeClusterDefinition(
                name="test-cluster@",
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
            )

    def test_no_worker_group(self):
        # Test Case: Check if object creation passes with no worker group
        cluster = ComputeClusterDefinition(
            name=self.name,
            tags=self.tags,
            labels=self.labels,
            runtime=self.runtime,
            head_node=self.head_node,
            worker_group=[],
            user=self.user,
        )
        self.assertEqual(cluster.worker_group, [])

    def test_user(self):
        # Test Case: Check of object creation passes with valid username
        cluster = ComputeClusterDefinition(
            name=self.name,
            tags=self.tags,
            labels=self.labels,
            runtime=self.runtime,
            head_node=self.head_node,
            worker_group=self.worker_group,
            user="testuser",
        )
        self.assertEqual(cluster.user, "testuser")

        # Test Case: Check if object creation passes with username as email
        cluster = ComputeClusterDefinition(
            name=self.name,
            tags=self.tags,
            labels=self.labels,
            runtime=self.runtime,
            head_node=self.head_node,
            worker_group=self.worker_group,
            user="harsh.a@dream11.com",
        )

        # Test case: Check if object creation fails with space in username
        with self.assertRaises(ValueError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                user="test user",
            )

        # Test Case: Check if object creation fails with special character in username
        with self.assertRaises(ValueError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                user="test@user",
            )

    def test_ray_start_params(self):
        # Test Case: Check if object creation passes with valid ray_start_params
        cluster = ComputeClusterDefinition(
            name=self.name,
            tags=self.tags,
            labels=self.labels,
            runtime=self.runtime,
            head_node=self.head_node,
            worker_group=self.worker_group,
            advance_config=AdvanceConfig(ray_start_params=RayStartParams(num_cpus_on_head=2)),
            user=self.user,
        )
        self.assertEqual(cluster.advance_config.ray_start_params.num_cpus_on_head, 2)

        # Test Case: Check if object creation fails with invalid ray_start_params
        with self.assertRaises(ValueError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                advance_config=AdvanceConfig(ray_start_params=RayStartParams(num_cpus_on_head=3)),
                user=self.user,
            )

        # Test Case: Check if object creation fails with gpus_on_head
        with self.assertRaises(ValueError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                advance_config=AdvanceConfig(ray_start_params=RayStartParams(num_gpus_on_head=1)),
                user=self.user,
            )

        # Test Case: Check if object creation fails with invalid object_store_memory_perc
        with self.assertRaises(ValueError):
            ComputeClusterDefinition(
                name=self.name,
                tags=self.tags,
                labels=self.labels,
                runtime=self.runtime,
                head_node=self.head_node,
                worker_group=self.worker_group,
                advance_config=AdvanceConfig(ray_start_params=RayStartParams(object_store_memory_perc=1)),
            )

    def test_convert(self):
        # Test Case: Check if the object is converted to the required format
        cluster = ComputeClusterDefinition(
            name=self.name,
            tags=self.tags,
            labels=self.labels,
            runtime=self.runtime,
            head_node=self.head_node,
            worker_group=self.worker_group,
            user=self.user,
        )
        expected_output = {
            "cluster_name": self.name,
            "tags": self.tags,
            "labels": self.labels,
            "runtime": self.runtime,
            "head_node_config": {
                "cores": 2,
                "memory": 4,
                "node_type": None,
                "disk_setting": None,
                "node_capacity_type": "ondemand",
            },
            "worker_node_configs": [
                {
                    "cores_per_pods": 2,
                    "memory_per_pods": 4,
                    "min_pods": 1,
                    "max_pods": 2,
                    "node_type": None,
                    "disk_setting": None,
                    "node_capacity_type": "ondemand",
                }
            ],
            "inactive_time": 60,
            "auto_termination_policies": [
                {"policy_name": "JupyterLabActivity", "enabled": True, "params": {}},
                {"policy_name": "ClusterCPUUsage", "enabled": True, "params": {}},
                {"policy_name": "ActiveRayJob", "enabled": True, "params": {}},
            ],
            "advance_config": {
                "environment_variables": "",
                "log_path": "",
                "init_script": "",
                "instance_role": {"id": 1, "display_name": "darwin-ds-role", "service_account_name": "darwin-ds-role"},
                "ray_params": {"object_store_memory": 25, "cpus_on_head": 0},
                "spark_config": {},
            },
            "user": self.user,
            "is_job_cluster": False,
            "start_cluster": True,
            "packages": None,
        }
        self.assertEqual(cluster.convert(), expected_output)
