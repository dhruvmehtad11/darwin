from datetime import datetime

from pydantic import BaseModel


class GetClusterSessionRequest(BaseModel):
    cluster_id: str
    start_timestamp: datetime
    end_timestamp: datetime
