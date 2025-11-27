import unittest

from compute_model.cpu_node import CPUNode
from compute_model.disk import Disk


class TestCPUNode(unittest.TestCase):
    def test_valid_object_creation(self):
        # Test case: Check if object creation is successful with valid types and values
        cores = 4
        memory = 8
        node = CPUNode(cores=cores, memory=memory)
        self.assertEqual(node.cores, cores)
        self.assertEqual(node.memory, memory)

    def test_passing_optional_fields(self):
        # Test case: Check if object creation is successful with optional fields
        cores = 4
        memory = 8
        disk = Disk(disk_type="gp2", disk_size=16)
        node_capacity_type = "spot"
        node_type = "general"
        node = CPUNode(
            cores=cores,
            memory=memory,
            disk=disk,
            node_capacity_type=node_capacity_type,
            node_type=node_type,
        )
        self.assertEqual(node.cores, cores)
        self.assertEqual(node.memory, memory)
        self.assertEqual(node.disk, disk)
        self.assertEqual(node.node_capacity_type, node_capacity_type)
        self.assertEqual(node.node_type, node_type)

    def test_object_creation_by_dict(self):
        # Test case: Check if object creation is successful with dictionary
        node = {
            "cores": 4,
            "memory": 8,
            "disk": {"disk_type": "gp2", "disk_size": 16},
            "node_capacity_type": "spot",
            "node_type": "general",
        }
        node_from_dict = CPUNode.from_dict(node)
        self.assertEqual(node_from_dict.cores, node["cores"])
        self.assertEqual(node_from_dict.memory, node["memory"])
        self.assertEqual(node_from_dict.disk, Disk.from_dict(node["disk"]))
        self.assertEqual(node_from_dict.node_capacity_type, node["node_capacity_type"])
        self.assertEqual(node_from_dict.node_type, node["node_type"])

    def test_invalid_object_type(self):
        # Test case: Check if object creation fails with invalid types
        cores = 4
        memory = 8

        with self.assertRaises(TypeError):
            CPUNode(cores="0", memory=memory)
        with self.assertRaises(TypeError):
            CPUNode(cores=cores, memory="0")
        with self.assertRaises(TypeError):
            CPUNode(cores=cores, memory=memory, disk="disk")
        with self.assertRaises(TypeError):
            CPUNode(cores=cores, memory=memory, node_capacity_type=0)
        with self.assertRaises(TypeError):
            CPUNode(cores=cores, memory=memory, node_type=0)

    def test_invalid_object_value(self):
        # Test case: Check if object creation fails with invalid values
        cores = 4
        memory = 8

        with self.assertRaises(ValueError):
            CPUNode(cores=0, memory=memory)
        with self.assertRaises(ValueError):
            CPUNode(cores=cores, memory=0)
        with self.assertRaises(ValueError):
            CPUNode(cores=cores, memory=memory, node_capacity_type="invalid")
        with self.assertRaises(ValueError):
            CPUNode(cores=cores, memory=memory, node_type="invalid")

    def test_invalid_cores(self):
        # Test case: Check if object creation fails with invalid cores and memory
        cores = 10000
        memory = 200
        with self.assertRaises(ValueError):
            CPUNode(cores=cores, memory=memory)

    def test_invalid_memory(self):
        # Test case: Check if object creation fails with invalid memory
        cores = 4
        memory = 1000000
        with self.assertRaises(ValueError):
            CPUNode(cores=cores, memory=memory)
