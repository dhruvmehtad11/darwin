from pydantic import BaseModel


class LaunchCodespaceRequest(BaseModel):
    project_id: int
    codespace_id: int
    user: str
