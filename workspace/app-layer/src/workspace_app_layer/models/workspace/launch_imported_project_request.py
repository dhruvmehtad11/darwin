from pydantic import BaseModel


class LaunchImportedProjectRequest(BaseModel):
    codespace_id: int
    github_url: str
