from pydantic import BaseModel


class DeleteProjectRequest(BaseModel):
    project_id: int
    user: str
