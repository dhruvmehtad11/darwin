from dataclasses import dataclass


@dataclass
class ClusterInfo:
    cluster_id: str
    dashboard_link: str
    jupyter_link: str
