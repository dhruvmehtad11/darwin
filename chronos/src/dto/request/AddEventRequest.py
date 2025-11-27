from typing import Optional

from pydantic import BaseModel, Field

from datetime import datetime


class AddEventRequest(BaseModel):
    source: str
    entity_id: str = Field(alias="entityId")
    event_type: str = Field(alias="eventType")
    timestamp: Optional[datetime] = datetime.now()
    details: dict
