from pydantic import BaseModel


class ClusterConfigResponse(BaseModel):
    value: str
