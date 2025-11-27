import datetime
from dataclasses import dataclass

from dataclasses_json import DataClassJsonMixin


@dataclass
class ClusterMetadata(DataClassJsonMixin):
    cluster_id: str
    cluster_name: str
    artifact_id: str
    last_updated_at: datetime.datetime
    last_used_at: datetime.datetime
    status: str = "inactive"
    active_cluster_runid: str = ""
    active_pods: int = 0
    available_memory: int = 0
