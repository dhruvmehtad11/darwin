import unittest

from mysql.connector import IntegrityError

from compute_app_layer.models.spark_history_server import (
    SparkHistoryServer,
    SparkHSStatus,
)
from compute_core.dao.mysql_dao import MySQLDao
from compute_core.dao.spark_history_server_dao import SparkHistoryServerDAO


class TestSparkHistoryServerDAO(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        mysql_dao = MySQLDao("test")
        mysql_dao.create(
            "INSERT INTO spark_history_server (id, resource, user, ttl, filesystem, events_path, cloud_env, status) "
            "VALUES ('shs-1', 'id-abc1', 'abc', 60, 's3', 's3a://path/id-abc1', 'local','created')",
            {},
        )
        mysql_dao.create(
            "INSERT INTO spark_history_server (id, resource, user, ttl, filesystem, events_path, cloud_env, status) "
            "VALUES ('shs-2', 'id-abc2', 'abc', 60, 's3', 's3a://path/id-abc2', 'local', 'active')",
            {},
        )
        mysql_dao.create(
            "INSERT INTO spark_history_server (id, resource, user, ttl, filesystem, events_path, cloud_env, status) "
            "VALUES ('shs-3', 'id-abc3', 'abc', 60, 's3', 's3a://path/id-abc3', 'local', 'inactive')",
            {},
        )
        mysql_dao.create(
            "INSERT INTO spark_history_server (id, resource, user, ttl, filesystem, events_path, cloud_env, status) "
            "VALUES ('shs-4', 'id-abc4', 'abc', 60, 's3', 's3a://path/id-abc4', 'local', 'inactive')",
            {},
        )

    @classmethod
    def tearDownClass(cls):
        mysql_dao = MySQLDao("test")
        mysql_dao.delete("DELETE FROM spark_history_server", {})

    def setUp(self):
        self._mysql_dao = SparkHistoryServerDAO("test")

    def test_get_all(self):
        limit, offset = 2, 0
        resp = self._mysql_dao.get_all(limit, offset)
        self.assertEqual(len(resp), 2)

    def test_get_all_active(self):
        limit, offset = 10, 0
        resp = self._mysql_dao.get_all_active(limit, offset)
        self.assertEqual(len(resp), 2)
        for r in resp:
            self.assertIn(r.status.value, ["active", "created"])

    def test_get_by_id(self):
        resp = self._mysql_dao.get_by_id("shs-2")
        self.assertEqual(resp.id, "shs-2")

        resp = self._mysql_dao.get_by_id("shs-unavailable")
        self.assertIsNone(resp)

    def test_get_by_resource(self):
        resp = self._mysql_dao.get_by_resource("id-abc2")
        self.assertEqual(resp.id, "shs-2")

        resp = self._mysql_dao.get_by_resource("id-abc-unavailable")
        self.assertIsNone(resp)

    def test_create(self):
        req = SparkHistoryServer(resource="id-abcnew", user="abc", cloud_env="local")
        self._mysql_dao.create(req)

        req1 = SparkHistoryServer(resource="id-abcnew", user="abc", cloud_env="local")
        with self.assertRaises(IntegrityError):
            self._mysql_dao.create(req1)

        self._mysql_dao.delete_by_id(req.id)

    def test_update(self):
        req = SparkHistoryServer(
            id="shs-3",
            resource="id-abc5",
            user="abc",
            cloud_env="local",
            status=SparkHSStatus.INACTIVE,
        )
        self._mysql_dao.update(req)

    def test_update_status(self):
        resp = self._mysql_dao.update_status("shs-3", SparkHSStatus.FAILED)
        self.assertEqual(resp, 1)

        resp = self._mysql_dao.update_status("shs-unavailable", SparkHSStatus.ACTIVE)
        self.assertEqual(resp, 0)

    def test_delete_by_id(self):
        resp = self._mysql_dao.delete_by_id("shs-4")
        self.assertEqual(resp, 1)

        resp = self._mysql_dao.delete_by_id("shs-unavailable")
        self.assertEqual(resp, 0)
