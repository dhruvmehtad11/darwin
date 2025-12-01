from pydantic import BaseModel


class UpdateUserEntity(BaseModel):
    cluster_user: str
