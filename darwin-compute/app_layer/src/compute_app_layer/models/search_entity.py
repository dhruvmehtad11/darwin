from pydantic import BaseModel
from typing import Optional


class SearchEntity(BaseModel):
    query: str = ""
    filters: dict
    exclude_filters: Optional[dict] = {}
    sort_by: str
    sort_order: str
    page_size: int
    offset: int
    is_job_cluster: Optional[bool] = False
