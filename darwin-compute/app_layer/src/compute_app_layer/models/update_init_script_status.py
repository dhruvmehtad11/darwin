from pydantic import BaseModel


class UpdateInitScriptStatusEntity(BaseModel):
    cluster_id: str
    status: str
    message: str
    uid: str
