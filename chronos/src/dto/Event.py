from datetime import datetime

from pydantic import BaseModel


class Event(BaseModel):
    source: str
    entity_id: str
    event_type: str
    timestamp: datetime
    details: dict
