import unittest

from compute_model.cpu_node import CPUNode
from compute_model.gpu_node import GPUNode
from compute_model.head_node import HeadNode


class TestHeadNode(unittest.TestCase):
    def test_valid_object_creation(self):
        # Test case: Check if object creation is successful with valid types and values
        # cpu_node = CPUNode(cores=4, memory=8)
        cpu_node = {
            "cores": 4,
            "memory": 8,
        }
        head_cpu_node = HeadNode(node=cpu_node)
        self.assertEqual(head_cpu_node.node.cores, cpu_node["cores"])
        self.assertEqual(head_cpu_node.node.memory, cpu_node["memory"])
        self.assertIsInstance(head_cpu_node.node, CPUNode)
        gpu_node = {
            "name": "NVIDIA T4",
            "cores": 40,
            "memory": 165,
            "gpu_count": 4,
            "g_ram_memory": 64,
            "g_ram_type": "GDDR6",
            "node_type": "gpu",
        }
        head_gpu_node = HeadNode(node=gpu_node, node_type="gpu")
        self.assertEqual(head_gpu_node.node.name, gpu_node["name"])
        self.assertEqual(head_gpu_node.node.cores, gpu_node["cores"])
        self.assertEqual(head_gpu_node.node.memory, gpu_node["memory"])
        self.assertEqual(head_gpu_node.node.gpu_count, gpu_node["gpu_count"])
        self.assertEqual(head_gpu_node.node.g_ram_memory, gpu_node["g_ram_memory"])
        self.assertEqual(head_gpu_node.node.g_ram_type, gpu_node["g_ram_type"])
        self.assertIsInstance(head_gpu_node.node, GPUNode)

    def test_object_creation_by_dict(self):
        # Test case: Check if object creation is successful with dictionary
        head_cpu_node = {
            "node": {
                "cores": 4,
                "memory": 8,
            }
        }
        head_cpu_node_from_dict = HeadNode.from_dict(head_cpu_node)
        self.assertIsInstance(head_cpu_node_from_dict.node, CPUNode)

        head_gpu_node = {
            "node": {
                "name": "NVIDIA T4",
                "cores": 40,
                "memory": 165,
                "gpu_count": 4,
                "g_ram_memory": 64,
                "g_ram_type": "GDDR6",
                "node_type": "gpu",
            },
            "node_type": "gpu",
        }

        head_gpu_node_from_dict = HeadNode.from_dict(head_gpu_node)
        self.assertIsInstance(head_gpu_node_from_dict.node, GPUNode)

    def test_invalid_object_type(self):
        # Test case: Check if object creation fails with invalid types
        cpu_node = {
            "cores": 4,
            "memory": 8,
        }
        node_type = "gpu"

        with self.assertRaises(TypeError):
            HeadNode(node=cpu_node, node_type=1)

        # with self.assertRaises(TypeError):
        #     HeadNode(node=cpu_node, node_type=node_type)

    def test_invalid_object_value(self):
        # Test case: Check if object creation fails with invalid values
        cpu_node = {
            "cores": 4,
            "memory": 8,
        }

        with self.assertRaises(ValueError):
            HeadNode(node=cpu_node, node_type="invalid_type")

    def test_convert_cpu_ondemand(self):
        # Test case: Check if CPU ondemand node object conversion is successful
        head_node = HeadNode(
            node={
                "cores": 4,
                "memory": 8,
            }
        )
        head_cpu_node_dict = head_node.convert()
        self.assertEqual(
            head_cpu_node_dict,
            {
                "cores": 4,
                "memory": 8,
                "node_type": None,
                "disk_setting": None,
                "node_capacity_type": "ondemand",
            },
        )

    def test_convert_cpu_spot(self):
        # Test case: Check if object conversion is successful
        head_node = HeadNode(
            node={
                "cores": 4,
                "memory": 8,
                "node_capacity_type": "spot",
                "node_type": "general",
            },
            node_type="general",
        )
        head_cpu_node_dict = head_node.convert()
        self.assertEqual(
            head_cpu_node_dict,
            {
                "cores": 4,
                "memory": 8,
                "node_type": "general",
                "disk_setting": None,
                "node_capacity_type": "spot",
            },
        )

    def test_convert_gpu(self):
        # Test case: Check if GPU Node Object conversion is successful
        head_gpu_node = {
            "node": {
                "name": "NVIDIA T4",
                "cores": 40,
                "memory": 165,
                "gpu_count": 4,
                "g_ram_memory": 64,
                "g_ram_type": "GDDR6",
                "node_type": "gpu",
            },
            "node_type": "gpu",
        }
        head_gpu_node_obj = HeadNode.from_dict(head_gpu_node)
        head_gpu_node_dict = head_gpu_node_obj.convert()
        self.assertEqual(
            head_gpu_node_dict,
            {
                "cores": 40,
                "memory": 165,
                "node_type": "gpu",
                "gpu_pod": head_gpu_node_obj.node.convert(),
            },
        )
