import os
from enum import Enum

CONFIGS_MAP = {
    "darwin-local": {"slack_channel": "darwin_auto_termination_alerts"},
    "local": {"slack_channel": "darwin_auto_termination_alerts"},
}

SLACK_TOKEN = os.getenv("VAULT_SERVICE_SLACK_TOKEN", "")
SLACK_USERNAME = "prd_ids alert"

RAY_JOBS_URL = "api/jobs/"
CLUSTER_NODES_SUMMARY_URL = "nodes?view=summary"

CLUSTER_DIED_STATUS_TIMEOUT_IN_MINS = 30

CLUSTER_MAX_CREATION_TIMEOUT_IN_MINS = 40

CLUSTER_STATUS = {"creating": "creating", "active": "active", "inactive": "inactive"}

CLUSTER_TYPES = {"gpu": "gpu", "cpu": "cpu"}

CLUSTER_DIED_ACTION = "Cluster Died"
HEAD_NODE_UP_ACTION = "Head Node Up"
HEAD_NODE_DIED_ACTION = "Head Node Died"
WORKER_NODES_UP_ACTION = "Worker Nodes Up"
ACTIVE_ACTION = "Running"
WORKER_NODES_DIED_ACTION = "Worker Nodes Died"
WORKER_NODES_SCALED_ACTION = "Worker Nodes Scaled"
CLUSTER_INACTIVE_ACTION = "Cluster Inactive"
CLUSTER_RESTART_ACTION = "Restarting"
CLUSTER_START_ACTION = "Started"
CLUSTER_STOP_ACTION = "Stopped"


class RemoteCommandErrorCode(Enum):
    REMOTE_COMMAND_FAILED = "REMOTE_COMMAND_FAILED"
