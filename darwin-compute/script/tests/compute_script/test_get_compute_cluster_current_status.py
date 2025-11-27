from unittest import TestCase

from compute_model.cluster_status import ClusterStatus
from compute_script.get_compute_cluster_state import get_compute_cluster_current_status
from compute_script.dto.compute_cluster_dto import Node


class TestGetComputeClusterCurrentStatus(TestCase):
    def test_get_compute_current_status_creating(self):
        compute_cluster_dto = Node()
        old_status = ClusterStatus["creating"]
        worker_nodes_required = 3
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["creating"])

    def test_get_compute_current_status_head_node_up(self):
        compute_cluster_dto = Node(
            head_node=1,
            jupyter=0,
            worker_nodes=2,
        )
        worker_nodes_required = 3
        old_status = ClusterStatus["creating"]
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["head_node_up"])

        old_status = ClusterStatus["jupyter_up"]
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["head_node_up"])

    def test_get_compute_current_status_jupyter_up(self):
        compute_cluster_dto = Node(
            head_node=1,
            jupyter=1,
            worker_nodes=2,
        )
        old_status = ClusterStatus["head_node_up"]
        worker_nodes_required = 3
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["jupyter_up"])

    def test_get_compute_current_status_active(self):
        compute_cluster_dto = Node(
            head_node=1,
            jupyter=1,
            worker_nodes=3,
        )
        worker_nodes_required = 3
        old_status = ClusterStatus["jupyter_up"]
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["active"])

    def test_get_compute_current_status_cluster_died(self):
        compute_cluster_dto = Node()
        old_status = ClusterStatus["jupyter_up"]
        worker_nodes_required = 3
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["cluster_died"])

    def test_get_compute_current_status_head_node_died(self):
        compute_cluster_dto = Node()
        compute_cluster_dto.worker_nodes = 2
        old_status = ClusterStatus["jupyter_up"]
        worker_nodes_required = 3
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["head_node_died"])

    def test_get_compute_current_status_worker_nodes_scaled(self):
        compute_cluster_dto = Node()
        compute_cluster_dto.head_node = 1
        compute_cluster_dto.jupyter = 1
        compute_cluster_dto.worker_nodes = 4
        old_status = ClusterStatus["jupyter_up"]
        worker_nodes_required = 3
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["worker_nodes_scaled"])

    def test_get_compute_current_status_worker_nodes_died(self):
        compute_cluster_dto = Node()
        compute_cluster_dto.head_node = 1
        compute_cluster_dto.jupyter = 1
        compute_cluster_dto.worker_nodes = 2
        old_status = ClusterStatus["active"]
        worker_nodes_required = 3
        status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
        self.assertEqual(status, ClusterStatus["worker_nodes_died"])

    def test_get_compute_current_status_node_null(self):
        with self.assertRaises(TypeError):
            compute_cluster_dto = Node(
                head_node=None,
                jupyter=None,
                worker_nodes=1,
            )
            old_status = ClusterStatus["jupyter_up"]
            worker_nodes_required = 3
            status = get_compute_cluster_current_status(compute_cluster_dto, old_status, worker_nodes_required)
            self.assertEqual(status, ClusterStatus["jupyter_up"])
