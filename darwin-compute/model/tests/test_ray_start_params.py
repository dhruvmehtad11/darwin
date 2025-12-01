import unittest

from compute_model.ray_start_params import RayStartParams


class TestRayStartParams(unittest.TestCase):
    def test_valid_object_creation(self):
        # Test case: Check if object creation is successful with valid types and values
        object_store_memory_perc = 50
        num_cpus_on_head = 2

        # Create the object
        params = RayStartParams(
            object_store_memory_perc=object_store_memory_perc,
            num_cpus_on_head=num_cpus_on_head,
        )

        # Assert the attributes
        self.assertEqual(params.object_store_memory_perc, object_store_memory_perc)
        self.assertEqual(params.num_cpus_on_head, num_cpus_on_head)
        self.assertEqual(params.num_gpus_on_head, 0)

    def test_valid_object_creation_from_dict(self):
        # Test case: Check if object creation is successful with valid values from dict
        params = {
            "object_store_memory_perc": 50,
            "num_cpus_on_head": 2,
            "num_gpus_on_head": 1,
        }
        ray_start_params = RayStartParams.from_dict(params)

        # Assert the attributes
        self.assertEqual(
            ray_start_params.object_store_memory_perc,
            params["object_store_memory_perc"],
        )
        self.assertEqual(ray_start_params.num_cpus_on_head, params["num_cpus_on_head"])
        self.assertEqual(ray_start_params.num_gpus_on_head, params["num_gpus_on_head"])

    def test_invalid_object_type(self):
        # Test case: Check if object creation raises TypeError for invalid types
        object_store_memory_perc = "50"  # Invalid type, should be int
        num_cpus_on_head = "2"  # Invalid type, should be int
        num_gpus_on_head = "1"  # Invalid type, should be int

        # Create the object
        with self.assertRaises(TypeError):
            RayStartParams(
                object_store_memory_perc=object_store_memory_perc,
            )

        with self.assertRaises(TypeError):
            RayStartParams(
                num_cpus_on_head=num_cpus_on_head,
            )

        with self.assertRaises(TypeError):
            RayStartParams(
                num_gpus_on_head=num_gpus_on_head,
            )

    def test_invalid_object_creation(self):
        # Test case: Check if object creation raises ValueError for invalid values
        object_store_memory_perc = 0  # Invalid value, should be greater than 0
        num_cpus_on_head = -1  # Invalid value, should be greater than or equal to 0
        num_gpus_on_head = -1  # Invalid value, should be greater than or equal to 0

        # Create the object
        with self.assertRaises(ValueError):
            RayStartParams(
                object_store_memory_perc=object_store_memory_perc,
            )

        with self.assertRaises(ValueError):
            RayStartParams(
                num_cpus_on_head=num_cpus_on_head,
            )

        with self.assertRaises(ValueError):
            RayStartParams(num_gpus_on_head=num_gpus_on_head)

    def test_convert(self):
        # Test case: Check if object is converted to dictionary
        ray_start_params = RayStartParams(object_store_memory_perc=50, num_cpus_on_head=2, num_gpus_on_head=1)
        ray_start_params_dict = ray_start_params.convert()
        self.assertEqual(
            ray_start_params_dict,
            {
                "object_store_memory": 50,
                "cpus_on_head": 2,
            },
        )
