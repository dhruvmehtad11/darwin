from dataclasses import dataclass
from typing import Optional

from dataclasses_json import DataClassJsonMixin


@dataclass
class ProjectResponse(DataClassJsonMixin):
    id: int
    name: str
    user_id: str
    created_at: str
    updated_at: str
    default_codespace: Optional[str] = None
    cloned_from: Optional[str] = None
