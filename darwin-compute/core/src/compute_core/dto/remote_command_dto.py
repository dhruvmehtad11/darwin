from enum import Enum
from dataclasses import dataclass
from dataclasses_json import DataClassJsonMixin


class RemoteCommandStatus(Enum):
    CREATED = "created"
    STARTED = "started"
    RUNNING = "running"
    SUCCESS = "success"
    FAILED = "failed"


class RemoteCommandTarget(Enum):
    head = "head"
    worker = "worker"
    cluster = "cluster"


@dataclass
class RemoteCommandDto(DataClassJsonMixin):
    execution_id: str
    command: str
    target: RemoteCommandTarget
    status: RemoteCommandStatus
    timeout: int = 300


@dataclass
class RemoteCommandExecuteDCMDto(DataClassJsonMixin):
    kube_cluster: str
    kube_namespace: str
    label_selector: str
    container_name: str
    command: str


@dataclass
class PodCommandExecutionStatusDto(DataClassJsonMixin):
    cluster_run_id: str
    execution_id: str
    pod_name: str
    status: str
