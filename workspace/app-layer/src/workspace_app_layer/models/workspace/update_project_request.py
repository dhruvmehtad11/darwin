from pydantic import BaseModel


class UpdateProjectRequest(BaseModel):
    project_id: int
    project_name: str
    user: str
