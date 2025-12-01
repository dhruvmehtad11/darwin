from pydantic import BaseModel


class UpdateCodespaceRequest(BaseModel):
    project_id: str
    codespace_name: str
    user: str
    codespace_id: str
