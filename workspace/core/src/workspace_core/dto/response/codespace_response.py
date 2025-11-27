from dataclasses import dataclass, field
from datetime import datetime
from typing import Optional

from dataclasses_json import DataClassJsonMixin, config


@dataclass
class CodespaceResponse(DataClassJsonMixin):
    id: int
    project_id: int
    name: str
    created_at: datetime = field(metadata=config(encoder=datetime.isoformat))
    updated_at: datetime = field(metadata=config(encoder=datetime.isoformat))
    last_synced_at: datetime = field(metadata=config(encoder=datetime.isoformat))
    user_id: Optional[str] = None
    cluster_id: Optional[str] = None
    jupyter_link: Optional[str] = None
    sync_location: Optional[str] = None
    sync_job_id: Optional[str] = None
