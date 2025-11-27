import unittest

from compute_model.cpu_node import CPUNode
from compute_model.gpu_node import GPUNode
from compute_model.worker_group import WorkerGroup


class TestWorkerGroup(unittest.TestCase):
    def test_valid_object_creation(self):
        # Test case: Check if object creation is successful with valid types and values
        cpu_node = {
            "cores": 4,
            "memory": 8,
        }
        min_pods = 1
        max_pods = 2
        wg = WorkerGroup(node=cpu_node, min_pods=min_pods, max_pods=max_pods)
        self.assertEqual(wg.node.cores, cpu_node["cores"])
        self.assertEqual(wg.node.memory, cpu_node["memory"])
        self.assertIsInstance(wg.node, CPUNode)
        self.assertEqual(wg.min_pods, min_pods)
        self.assertEqual(wg.max_pods, max_pods)

    def test_valid_object_creation_from_dict(self):
        # Test case: Check if object creation is successful with valid types and values
        worker_cpu_node = {
            "node": {
                "cores": 4,
                "memory": 8,
            },
            "min_pods": 1,
            "max_pods": 2,
        }
        wg = WorkerGroup.from_dict(worker_cpu_node)
        self.assertEqual(wg.node.cores, worker_cpu_node["node"]["cores"])
        self.assertEqual(wg.node.memory, worker_cpu_node["node"]["memory"])
        self.assertIsInstance(wg.node, CPUNode)
        self.assertEqual(wg.min_pods, worker_cpu_node["min_pods"])
        self.assertEqual(wg.max_pods, worker_cpu_node["max_pods"])
        worker_gpu_node = {
            "node": {
                "name": "NVIDIA T4",
                "cores": 40,
                "memory": 165,
                "gpu_count": 4,
                "g_ram_memory": 64,
                "g_ram_type": "GDDR6",
                "node_type": "gpu",
            },
            "min_pods": 1,
            "max_pods": 2,
            "node_type": "gpu",
        }
        wg = WorkerGroup.from_dict(worker_gpu_node)
        self.assertIsInstance(wg.node, GPUNode)
        self.assertEqual(wg.max_pods, worker_gpu_node["max_pods"])
        self.assertEqual(wg.min_pods, worker_gpu_node["min_pods"])
        self.assertEqual(wg.node_type, worker_gpu_node["node_type"])

    def test_invalid_object_type(self):
        # Test case: Check if object creation fails with invalid types
        cpu_node = {
            "cores": 4,
            "memory": 8,
        }
        min_pods = 1
        max_pods = 2
        with self.assertRaises(TypeError):
            WorkerGroup(node=cpu_node, min_pods="1", max_pods=max_pods)
        with self.assertRaises(TypeError):
            WorkerGroup(node=cpu_node, min_pods=min_pods, max_pods="2")
        with self.assertRaises(TypeError):
            WorkerGroup(node=cpu_node, min_pods=min_pods, max_pods=max_pods, node_type=1)

    def test_invalid_object_value(self):
        # Test case: Check if object creation fails with invalid values
        cpu_node = {
            "cores": 4,
            "memory": 8,
        }
        min_pods = 1
        max_pods = 2
        with self.assertRaises(ValueError):
            WorkerGroup(node=cpu_node, min_pods=-1, max_pods=max_pods)
        with self.assertRaises(ValueError):
            WorkerGroup(node=cpu_node, min_pods=min_pods, max_pods=0)
        with self.assertRaises(ValueError):
            WorkerGroup(node=cpu_node, min_pods=2, max_pods=1)
        with self.assertRaises(ValueError):
            WorkerGroup(node=cpu_node, min_pods=min_pods, max_pods=max_pods, node_type="invalid")

    def test_convert_cpu_ondemand(self):
        # Test case: Check if convert method returns the correct dict for CPU Ondemand
        cpu_node = {
            "cores": 4,
            "memory": 8,
        }
        min_pods = 1
        max_pods = 2
        wg = WorkerGroup(node=cpu_node, min_pods=min_pods, max_pods=max_pods)
        self.assertEqual(
            wg.convert(),
            {
                "cores_per_pods": cpu_node["cores"],
                "memory_per_pods": cpu_node["memory"],
                "min_pods": min_pods,
                "max_pods": max_pods,
                "node_type": None,
                "disk_setting": None,
                "node_capacity_type": "ondemand",
            },
        )

    def test_convert_cpu_spot(self):
        # Test case: Check if convert method returns the correct dict for CPU Spot
        cpu_node = {
            "cores": 4,
            "memory": 8,
            "node_capacity_type": "spot",
            "node_type": "general",
        }
        min_pods = 1
        max_pods = 2
        wg = WorkerGroup(node=cpu_node, min_pods=min_pods, max_pods=max_pods, node_type="general")
        self.assertEqual(
            wg.convert(),
            {
                "cores_per_pods": cpu_node["cores"],
                "memory_per_pods": cpu_node["memory"],
                "min_pods": min_pods,
                "max_pods": max_pods,
                "node_type": cpu_node["node_type"],
                "disk_setting": None,
                "node_capacity_type": cpu_node["node_capacity_type"],
            },
        )

    def test_convert_gpu(self):
        # Test case: Check if convert method returns the correct dict for GPU Node
        min_pods = 1
        max_pods = 2
        gpu_node = {
            "name": "NVIDIA T4",
            "cores": 40,
            "memory": 165,
            "gpu_count": 4,
            "g_ram_memory": 64,
            "g_ram_type": "GDDR6",
            "node_type": "gpu",
        }
        wg = WorkerGroup(node=gpu_node, min_pods=min_pods, max_pods=max_pods, node_type="gpu")
        self.assertEqual(
            wg.convert(),
            {
                "cores_per_pods": gpu_node["cores"],
                "memory_per_pods": gpu_node["memory"],
                "min_pods": min_pods,
                "max_pods": max_pods,
                "node_type": "gpu",
                "gpu_pod": wg.node.convert(),
            },
        )
