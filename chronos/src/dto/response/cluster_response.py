from datetime import datetime
from typing import List, Optional

from pydantic import BaseModel

from src.dto.schema import ProcessedEventResponse


class ClusterSession(BaseModel):
    session_id: Optional[str] = None
    start_timestamp: datetime
    end_timestamp: Optional[datetime] = None


class ClusterSessionsResponse(BaseModel):
    data: List[ClusterSession]


class ClusterEventSource(BaseModel):
    id: int
    label: str


class ClusterEventSourcesResponse(BaseModel):
    data: List[ClusterEventSource]


class ClusterEventData(BaseModel):
    raw_event_id: int
    processed_event_id: int
    timestamp: datetime
    event_type: str
    message: Optional[str] = None
    severity: Optional[str] = None
    source_id: int


class ClusterEventsResponse(BaseModel):
    page_size: int
    offset: int
    data: List[ClusterEventData]


class ClusterProcessedEvent(BaseModel):
    processed_event_id: int
    event_data: ProcessedEventResponse
    timestamp: datetime


class GetProcessedEventResponse(BaseModel):
    data: ClusterProcessedEvent
