from pydantic import BaseModel


class RecentlyVisitedRequest(BaseModel):
    cluster_id: str
