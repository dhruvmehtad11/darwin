from pydantic import BaseModel


class GetAllClusterRequest(BaseModel):
    search_string: str
    offset: int
    page_size: int
