import unittest
from unittest.mock import patch, MagicMock

import pytest

from compute_core.dao.cluster_dao import ClusterDao
from compute_core.dto.exceptions import ClusterNotFoundError, ClusterRunIdNotFoundError


class TestClusterDao(unittest.TestCase):
    @patch("compute_core.dao.cluster_dao.MySQLDao")
    @patch("compute_core.dao.cluster_dao.ESDao")
    def setUp(self, mock_es_dao, mock_mysql_dao):
        self.mock_es_dao = mock_es_dao.return_value
        self.mock_mysql_dao = mock_mysql_dao.return_value
        self.fake_connection = MagicMock()
        self.mock_mysql_dao.get_read_connection.return_value = self.fake_connection
        self.cluster_dao = ClusterDao()

    @pytest.fixture(autouse=True)
    def prepare_fixture(self, basic_compute_cluster_def, es_compute_def):
        self.basic_compute_cluster_def = basic_compute_cluster_def
        self.es_compute_def = es_compute_def

    def test_healthcheck(self):
        self.mock_mysql_dao.healthcheck.return_value = True
        self.mock_es_dao.healthcheck.return_value = True
        assert self.cluster_dao.healthcheck()

    def test_search_cluster_name(self):
        self.mock_es_dao.aggregation_search.return_value = {
            "took": 1,
            "timed_out": False,
            "_shards": {"total": 1, "successful": 1, "skipped": 0, "failed": 0},
            "hits": {"total": {"value": 0, "relation": "eq"}, "max_score": None, "hits": []},
        }

        cluster = self.cluster_dao.search_cluster_name("test")
        self.assertEqual(cluster, self.mock_es_dao.aggregation_search.return_value)

    def test_get_cluster_status(self):
        self.mock_mysql_dao.read.return_value = [{"status": "inactive"}]
        status = self.cluster_dao.get_cluster_status("id-karimqrsgf7a6at3wf4")
        self.assertEqual(status, "inactive")

        self.mock_mysql_dao.read.return_value = []
        with self.assertRaises(Exception) as e:
            self.cluster_dao.get_cluster_status("id-test")

    def test_get_cluster_artifact_id(self):
        self.mock_mysql_dao.read.return_value = [{"artifact_id": "id-karimqrsgf7a6at3wf4-v1"}]
        artifact_id = self.cluster_dao.get_cluster_artifact_id("id-karimqrsgf7a6at3wf4")
        self.assertEqual(artifact_id, "id-karimqrsgf7a6at3wf4-v1")

    def test_get_cluster_run_id(self):
        self.mock_mysql_dao.read.return_value = [{"run_id": "run-karimqrsgf7a6at3wf4v1"}]
        run_id = self.cluster_dao.get_cluster_run_id("id-karimqrsgf7a6at3wf4")
        self.assertEqual(run_id, "run-karimqrsgf7a6at3wf4v1")

    def test_get_cluster_run_id_v2(self):
        # When cluster_id exists
        self.fake_connection.cursor.fetchone.return_value = {"run_id": "run_id-test"}
        run_id = self.cluster_dao.get_cluster_run_id_v2("id-test")
        self.assertEqual(run_id, "run_id-test")

        # When cluster_id does not exist
        self.fake_connection.cursor.fetchone.return_value = None
        with self.assertRaises(ClusterNotFoundError):
            self.cluster_dao.get_cluster_run_id_v2("id-test")

        # When cluster_id exists but run_id is None
        self.fake_connection.cursor.fetchone.return_value = {"run_id": None}
        with self.assertRaises(ClusterRunIdNotFoundError):
            self.cluster_dao.get_cluster_run_id_v2("id-test")

    def test_create_cluster(self):
        self.mock_mysql_dao.create.return_value = (1, "Success")
        cluster = self.cluster_dao.create_cluster("id-test", "id-test-v1", self.basic_compute_cluster_def)
        self.assertEqual(cluster, (1, "Success"))

    def test_update_cluster_user(self):
        cluster_id = "test_id"
        cluster_user = "test_user"
        self.mock_es_dao.read.return_value = self.es_compute_def
        self.mock_es_dao.update.return_value = "test_value"
        user = self.cluster_dao.update_cluster_user(cluster_id, cluster_user)
        self.assertEqual(user, "test_value")

    def test_get_clusters_last_used_before_days(self):
        # test with non-empty cluster_id with only 1 input
        job_cluster_id_list = ["test_cluster_id"]
        self.mock_mysql_dao.read.return_value = [{"cluster_id": "test_value"}]
        response = self.cluster_dao.get_clusters_last_used_before_days(days=1, cluster_ids=job_cluster_id_list)
        expected = [{"cluster_id": "test_value"}]
        self.assertEqual(expected, response)

        # test with non-empty cluster_id with more than 1 input
        job_cluster_id_list = ["test_cluster_id_1", "test_cluster_id_2"]
        self.mock_mysql_dao.read.return_value = [{"cluster_id": "test_value"}]
        response = self.cluster_dao.get_clusters_last_used_before_days(days=1, cluster_ids=job_cluster_id_list)
        expected = [{"cluster_id": "test_value"}]
        self.assertEqual(expected, response)

        # test with empty cluster_id input
        job_cluster_id_list = []
        self.mock_mysql_dao.read.return_value = [{"cluster_id": "test_value"}]
        response = self.cluster_dao.get_clusters_last_used_before_days(days=1, cluster_ids=job_cluster_id_list)
        expected = []
        self.assertEqual(expected, response)

    def test_get_job_cluster_ids(self):
        self.mock_es_dao.aggregation_search.return_value = {"hits": {"hits": [{"_id": "test_cluster_id"}]}}
        response = self.cluster_dao.get_job_cluster_ids(offset=1, limit=1)
        expected = ["test_cluster_id"]
        self.assertEqual(expected, response)

    def test_get_all_cluster_config(self):
        test_value = "test"
        self.mock_mysql_dao.read.return_value = test_value
        expected = test_value
        res = self.cluster_dao.get_all_cluster_config(offset=0, limit=1)
        self.assertEqual(expected, res)

    # def test_get_cluster(self):
    #     cluster_dao = ClusterDao()
    #     cluster = cluster_dao.get_cluster(1)
    #     assert cluster["cluster_id"] == 1
    #     assert cluster["status"] == "active"
    #     assert cluster["head_node_config"]["head_node_cores"] == 4
    #     assert cluster["head_node_config"]["head_node_memory"] == 16
    #     assert cluster["worker_node_configs"][0]["cores"] == 2
    #     assert cluster["worker_node_configs"][0]["memory"] == 8
    #     assert cluster["worker_node_configs"][0]["min_pods"] == 1
