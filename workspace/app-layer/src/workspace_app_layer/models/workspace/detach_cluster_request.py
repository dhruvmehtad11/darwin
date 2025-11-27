from pydantic import BaseModel


class DetachClusterRequest(BaseModel):
    codespace_id: int
    cluster_id: str
    user: str
