from dataclasses import dataclass
from typing import Optional


@dataclass
class ClusterUsage:
    cores_used: float
    memory_used: float


@dataclass
class AttachedCluster:
    cluster_id: str
    cluster_name: str
    cluster_status: str
    memory: float
    cores: float
    attached_codespaces_count: int
    ray_dashboard: str
    prometheus_dashboard: str
    cluster_usage: Optional[ClusterUsage] = None
    spark_ui_dashboard: Optional[str] = None


@dataclass
class LaunchCodespaceResponse:
    project_id: int
    project_name: str
    codespace_id: int
    codespace_name: str
    github_link: Optional[str] = None
    last_sync_time: Optional[str] = None
    jupyter_lab_link: Optional[str] = None
    attached_cluster: Optional[AttachedCluster] = None
    code_server_link: Optional[str] = None


@dataclass
class CodespacePathDetailsResponse:
    user_id: str
    project_name: str
    codespace_name: str
