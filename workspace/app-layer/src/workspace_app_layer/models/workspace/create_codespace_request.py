from pydantic import BaseModel
from typing import Optional


class CreateCodespaceRequest(BaseModel):
    project_id: str
    codespace_name: str
    user: str
    cluster_id: str = None
    clone_from_codespace_name: Optional[str] = None
