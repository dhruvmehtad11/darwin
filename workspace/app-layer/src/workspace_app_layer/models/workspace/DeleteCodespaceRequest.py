from pydantic import BaseModel


class DeleteCodespaceRequest(BaseModel):
    project_id: int
    codespace_id: int
    user: str
