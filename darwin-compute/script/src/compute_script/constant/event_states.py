from enum import Enum


class AutoTerminationState(Enum):
    AUTO_TERMINATED = "AUTO_TERMINATED"


class MonitoringState(Enum):
    HEAD_NODE_UP = "HEAD_NODE_UP"
    WORKER_NODES_UP = "WORKER_NODES_UP"
    JUPYTER_UP = "JUPYTER_UP"
    CLUSTER_READY = "CLUSTER_READY"
    CLUSTER_INACTIVE = "CLUSTER_INACTIVE"


class ClusterTimeoutState(Enum):
    CLUSTER_TIMEOUT = "CLUSTER_TIMEOUT"
