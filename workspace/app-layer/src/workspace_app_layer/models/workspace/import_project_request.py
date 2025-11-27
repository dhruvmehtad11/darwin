from pydantic import BaseModel


class ImportProjectRequest(BaseModel):
    github_url: str
    codespace_name: str
    user: str
    cluster_id: str
