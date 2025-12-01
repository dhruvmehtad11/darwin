from unittest import TestCase

from compute_core.dao.jupyter_dao import JupyterDao


# TODO add assert statements - ujjawal
class TestJupyterDao(TestCase):
    def setUp(self):
        self.jupyter_dao = JupyterDao("test")

    def test_delete_pod_details(self):
        resp = self.jupyter_dao.delete_pod_details("abc")

    def test_get_unused_pods(self):
        resp = self.jupyter_dao.get_unused_pods()

    def test_get_unused_pods_count(self):
        resp = self.jupyter_dao.get_unused_pods_count()

    def test_get_pod_by_consumer_id(self):
        resp = self.jupyter_dao.get_pod_by_consumer_id("abc")
