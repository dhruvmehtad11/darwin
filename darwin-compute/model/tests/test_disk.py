import unittest

from compute_model.disk import Disk


class TestDisk(unittest.TestCase):
    def test_valid_object_creation(self):
        # Test case: Check if object creation is successful with valid types and values
        disk_type = "gp2"
        disk_size = 100

        # Create the object
        disk = Disk(disk_type=disk_type, disk_size=disk_size)

        # Assert the attributes
        self.assertEqual(disk.disk_type, disk_type)
        self.assertEqual(disk.disk_size, disk_size)

    def test_valid_object_creation_from_dict(self):
        # Test case: Check if object creation is successful with valid values
        disk = {"disk_type": "gp2", "disk_size": 100}
        obj = Disk.from_dict(disk)
        self.assertEqual(obj.disk_type, disk["disk_type"])
        self.assertEqual(obj.disk_size, disk["disk_size"])

    def test_invalid_type(self):
        # Test case: Check if object creation raises a TypeError for invalid types
        disk_type = "gp2"  # Valid literal type
        disk_size = "200"

        # Create the object
        with self.assertRaises(TypeError):
            Disk(disk_type=disk_type, disk_size=disk_size)

        with self.assertRaises(TypeError):
            Disk(disk_type=10, disk_size=100)

    def test_invalid_disk_type(self):
        # Test case: Check if object creation raises a ValueError for invalid disk_type
        disk_type = "invalid_disk_type"
        disk_size = 100

        # Create the object
        with self.assertRaises(ValueError):
            Disk(disk_type=disk_type, disk_size=disk_size)

    def test_invalid_disk_size(self):
        # Test case: Check if object creation raises a ValueError for invalid disk_size
        disk_type = "gp2"
        disk_size = -100

        # Create the object
        with self.assertRaises(ValueError):
            Disk(disk_type=disk_type, disk_size=disk_size)

    def test_convert(self):
        # Test case: Check if convert method returns the correct dict
        disk_type = "gp2"
        disk_size = 100

        # Create the object
        disk = Disk(disk_type=disk_type, disk_size=disk_size)

        # Convert to dict
        disk_dict = disk.convert()

        # Assert the dict
        self.assertEqual(disk_dict, {"disk_type": disk_type, "storage_size": disk_size})
