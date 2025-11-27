from pydantic import BaseModel


class CreateProjectRequest(BaseModel):
    project_name: str
    codespace_name: str
    user: str
    cluster_id: str = None
