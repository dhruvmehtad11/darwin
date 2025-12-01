import unittest

from compute_app_layer.models.spark_history_server import SparkHistoryServer


class TestSparkHistoryServer(unittest.TestCase):
    def test_successful_creation(self):
        obj = SparkHistoryServer(resource="test", user="test")
        self.assertEqual(obj.resource, "test")

    def test_required_fields(self):
        with self.assertRaises(ValueError):
            SparkHistoryServer()
        with self.assertRaises(ValueError):
            SparkHistoryServer(resource="test")
        with self.assertRaises(ValueError):
            SparkHistoryServer(user="test")
        SparkHistoryServer(resource="test", user="test")

    def test_default_values(self):
        obj = SparkHistoryServer(resource="test", user="test")
        self.assertEqual(obj.ttl, 60)
        self.assertEqual(obj.filesystem.value, "s3")
        self.assertEqual(obj.status.value, "created")
        self.assertTrue(obj.id.startswith("shs-"))

    def test_events_path(self):
        obj = SparkHistoryServer(resource="test", user="test")
        self.assertEqual(obj.events_path.startswith("s3a://"), True)
        obj = SparkHistoryServer(resource="test", user="test", filesystem="efs")
        self.assertEqual(obj.events_path, "fsx/spark/eventlogs/test")
        obj = SparkHistoryServer(resource="test", user="test", filesystem="s3", events_path="s3://test")
        self.assertEqual(obj.events_path, "s3a://test")
        obj = SparkHistoryServer(resource="test", user="test", filesystem="s3", events_path="test/test")
        self.assertEqual(obj.events_path, "s3a://test/test")
        obj = SparkHistoryServer(resource="test", user="test", filesystem="s3", events_path="/test/test")
        self.assertEqual(obj.events_path, "s3a://test/test")
        obj = SparkHistoryServer(resource="test", user="test", filesystem="s3", events_path="s3a://test/test")
        self.assertEqual(obj.events_path, "s3a://test/test")

    def test_dict_conversion(self):
        obj = SparkHistoryServer(resource="test", user="test")
        obj_dict = obj.model_dump()
        self.assertIsInstance(obj_dict, dict)
        self.assertEqual(obj_dict["resource"], "test")
        self.assertEqual(obj_dict["user"], "test")
        self.assertEqual(obj_dict["ttl"], 60)
        self.assertEqual(obj_dict["filesystem"], "s3")
        self.assertEqual(obj_dict["status"], "created")
