from typing import Optional

from pydantic import BaseModel


class JupyterPodRequest(BaseModel):
    consumer_id: str


class SearchRequest(BaseModel):
    query: str = ""
    filters: dict
    sort_by: str
    sort_order: str
    page_size: int
    offset: int
    is_job_cluster: Optional[bool] = False
