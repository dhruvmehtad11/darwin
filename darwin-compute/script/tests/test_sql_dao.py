import time
import unittest

from compute_script.dao.sql_dao import ScriptMySQLDao


class ScriptMySQLDaoTest(unittest.TestCase):
    def test_get_unpicked_cluster(self):
        dao = ScriptMySQLDao("stag")
        clusters = []
        start = time.time()
        while time.time() - start < 10:
            cluster = dao.get_unpicked_cluster()
            if cluster:
                clusters.append(cluster.cluster_id)
        self.assertEqual(len(set(clusters)), len(clusters))

    def test_get_unpicked_cluster_parallely(self):
        # Call get_unpicked_cluster in parallel and check that no cluster is picked more than once
        dao = ScriptMySQLDao("stag")
        import threading

        clusters = []

        def get_cluster():
            cluster = dao.get_unpicked_cluster()
            if cluster:
                clusters.append(cluster.cluster_id)

        start = time.time()
        threads = []
        while time.time() - start < 10:
            for i in range(10):
                threads.append(threading.Thread(target=get_cluster))
            for thread in threads:
                thread.start()
            for thread in threads:
                thread.join()
        self.assertEqual(len(set(clusters)), len(clusters))
