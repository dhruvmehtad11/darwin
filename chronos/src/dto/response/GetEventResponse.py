import json
from datetime import datetime

from pydantic import BaseModel, Field


class GetEventResponse(BaseModel):
    eventId: int = Field(alias="event_id")
    source: str
    entityId: str = Field(alias="entity_id")
    eventType: str = Field(alias="event_type")
    timestamp: datetime
    details: dict

    def __init__(self, **data):
        if 'details' in data and isinstance(data['details'], str):
            data['details'] = json.loads(data['details'])
        super().__init__(**data)

