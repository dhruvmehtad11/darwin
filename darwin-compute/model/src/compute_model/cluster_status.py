from enum import Enum


class ClusterStatus(Enum):
    inactive = "inactive"
    creating = "creating"
    head_node_up = "head_node_up"
    jupyter_up = "jupyter_up"
    active = "active"
    head_node_died = "head_node_died"
    worker_nodes_died = "worker_nodes_died"
    cluster_died = "cluster_died"
    worker_nodes_scaled = "worker_nodes_scaled"


class UIClusterStatusMapping(Enum):
    inactive = [ClusterStatus.inactive]
    creating = [
        ClusterStatus.creating,
        ClusterStatus.head_node_up,
        ClusterStatus.jupyter_up,
        ClusterStatus.head_node_died,
        ClusterStatus.cluster_died,
    ]
    active = [
        ClusterStatus.active,
        ClusterStatus.worker_nodes_scaled,
        ClusterStatus.worker_nodes_died,
    ]

    @classmethod
    def get_status(cls, status: ClusterStatus) -> str:
        for ui_status in cls:
            if status in ui_status.value:
                return ui_status.name
