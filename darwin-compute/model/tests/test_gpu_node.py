import unittest

from compute_model.gpu_node import GPUNode


class TestGPUNode(unittest.TestCase):
    def test_valid_object_creation(self):
        # Test case: Check if object creation is successful with valid types and values
        name = "NVIDIA T4"
        cores = 40
        memory = 165
        gpu_count = 4
        g_ram_memory = 64
        g_ram_type = "GDDR6"
        gpu_node = GPUNode(
            name=name,
            cores=cores,
            memory=memory,
            gpu_count=gpu_count,
            g_ram_memory=g_ram_memory,
            g_ram_type=g_ram_type,
        )
        self.assertEqual(gpu_node.name, name)
        self.assertEqual(gpu_node.cores, cores)
        self.assertEqual(gpu_node.memory, memory)
        self.assertEqual(gpu_node.gpu_count, gpu_count)
        self.assertEqual(gpu_node.g_ram_memory, g_ram_memory)
        self.assertEqual(gpu_node.g_ram_type, g_ram_type)
        self.assertEqual(gpu_node.node_type, "gpu")

    def test_object_creation_by_dict(self):
        # Test case: Check if object creation is successful with dictionary
        node = {
            "name": "NVIDIA T4",
            "cores": 40,
            "memory": 165,
            "gpu_count": 4,
            "g_ram_memory": 64,
            "g_ram_type": "GDDR6",
            "node_type": "gpu",
        }
        gpu_node_from_dict = GPUNode.from_dict(node)
        self.assertEqual(gpu_node_from_dict.name, node["name"])
        self.assertEqual(gpu_node_from_dict.cores, node["cores"])
        self.assertEqual(gpu_node_from_dict.memory, node["memory"])
        self.assertEqual(gpu_node_from_dict.gpu_count, node["gpu_count"])
        self.assertEqual(gpu_node_from_dict.g_ram_memory, node["g_ram_memory"])
        self.assertEqual(gpu_node_from_dict.g_ram_type, node["g_ram_type"])
        self.assertEqual(gpu_node_from_dict.node_type, "gpu")

    def test_invalid_object_type(self):
        # Test case: Check if object creation fails with invalid types
        name = "NVIDIA T4"
        cores = 30
        memory = 110
        gpu_count = 2
        g_ram_memory = 32
        g_ram_type = "GDDR6"

        with self.assertRaises(TypeError):
            GPUNode(
                name=1,
                cores=cores,
                memory=memory,
                gpu_count=gpu_count,
                g_ram_memory=g_ram_memory,
                g_ram_type=g_ram_type,
            )
        with self.assertRaises(TypeError):
            GPUNode(
                name=name,
                cores="0",
                memory=memory,
                gpu_count=gpu_count,
                g_ram_memory=g_ram_memory,
                g_ram_type=g_ram_type,
            )
        with self.assertRaises(TypeError):
            GPUNode(
                name=name,
                cores=cores,
                memory="0",
                gpu_count=gpu_count,
                g_ram_memory=g_ram_memory,
                g_ram_type=g_ram_type,
            )
        with self.assertRaises(TypeError):
            GPUNode(
                name=name,
                cores=cores,
                memory=memory,
                gpu_count="0",
                g_ram_memory=g_ram_memory,
                g_ram_type=g_ram_type,
            )
        with self.assertRaises(TypeError):
            GPUNode(
                name=name,
                cores=cores,
                memory=memory,
                gpu_count=gpu_count,
                g_ram_memory="0",
                g_ram_type=g_ram_type,
            )
        with self.assertRaises(TypeError):
            GPUNode(
                name=name,
                cores=cores,
                memory=memory,
                gpu_count=gpu_count,
                g_ram_memory=g_ram_memory,
                g_ram_type=0,
            )

    def test_invalid_object_value(self):
        name = "NVIDIA A100"
        cores = 4
        memory = 8
        gpu_count = 1
        g_ram_memory = 40
        g_ram_type = "HBM2"

        with self.assertRaises(ValueError):
            node = GPUNode(
                name=name,
                cores=cores,
                memory=memory,
                gpu_count=gpu_count,
                g_ram_memory=g_ram_memory,
                g_ram_type=g_ram_type,
            )
            # node.verify()

    def test_verify(self):
        # Test case: Check if verify method raises ValueError for invalid values
        name = "NVIDIA T4"
        cores = 40
        memory = 165
        gpu_count = 4
        g_ram_memory = 64
        g_ram_type = "GDDR6"
        gpu_node = GPUNode(
            name=name,
            cores=cores,
            memory=memory,
            gpu_count=gpu_count,
            g_ram_memory=g_ram_memory,
            g_ram_type=g_ram_type,
        )
        gpu_node.verify()
        gpu_node.name = "ABC"
        with self.assertRaises(ValueError):
            gpu_node.verify()

    def test_convert(self):
        # Test case: Check if convert method returns the correct dict
        name = "NVIDIA T4"
        cores = 40
        memory = 165
        gpu_count = 4
        g_ram_memory = 64
        g_ram_type = "GDDR6"
        gpu_node = GPUNode(
            name=name,
            cores=cores,
            memory=memory,
            gpu_count=gpu_count,
            g_ram_memory=g_ram_memory,
            g_ram_type=g_ram_type,
        )
        self.assertEqual(
            gpu_node.convert(),
            {
                "name": name,
                "cores": cores,
                "memory": memory,
                "gpu_count": gpu_count,
                "g_ram_memory": g_ram_memory,
                "g_ram_type": g_ram_type,
            },
        )
