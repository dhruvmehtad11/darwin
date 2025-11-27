from pydantic import BaseModel


class AttachClusterRequest(BaseModel):
    codespace_id: int
    cluster_id: str
    project_id: int
    user: str
