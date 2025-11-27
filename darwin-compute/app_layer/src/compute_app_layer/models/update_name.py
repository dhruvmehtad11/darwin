from pydantic import BaseModel


class UpdateNameEntity(BaseModel):
    cluster_name: str
