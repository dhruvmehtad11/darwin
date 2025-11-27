from pydantic import BaseModel


class ClusterConfig(BaseModel):
    key: str
    value: str


class ClusterConfigUpdateRequest(BaseModel):
    value: str
