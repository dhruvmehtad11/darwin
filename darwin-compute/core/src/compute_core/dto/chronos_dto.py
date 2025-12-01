from dataclasses import dataclass, field
from typing import Optional

from dataclasses_json import DataClassJsonMixin

from compute_core.util.utils import get_utc_time


@dataclass
class ChronosEvent(DataClassJsonMixin):
    event_type: str
    timestamp: str = field(default_factory=get_utc_time)
    cluster_id: Optional[str] = None
    cluster_name: Optional[str] = None
    session_id: Optional[str] = None
    message: Optional[str] = None
    artifact_name: Optional[str] = None
    metadata: Optional[dict] = None
