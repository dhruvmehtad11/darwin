import unittest

from compute_model.cluster_status import ClusterStatus, UIClusterStatusMapping


class TestClusterStatusEnums(unittest.TestCase):
    def test_ui_cluster_status_get_status(self):
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.inactive), "inactive")
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.creating), "creating")
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.head_node_up), "creating")
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.jupyter_up), "creating")
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.active), "active")
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.head_node_died), "creating")
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.worker_nodes_died), "active")
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.cluster_died), "creating")
        self.assertEqual(UIClusterStatusMapping.get_status(ClusterStatus.worker_nodes_scaled), "active")
