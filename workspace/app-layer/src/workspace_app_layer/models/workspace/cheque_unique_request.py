from pydantic import BaseModel


class CheckUniqueProjectNameRequest(BaseModel):
    project_name: str
    user: str


class CheckUniqueGithubLinkRequest(BaseModel):
    github_link: str
    user: str


class CheckUniqueCodespaceNameRequest(BaseModel):
    project_id: int
    codespace_name: str
    user: str
