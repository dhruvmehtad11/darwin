from datetime import datetime
from typing import Optional, Dict, List

from pydantic import BaseModel


class ProcessedEventBase(BaseModel):
    RawEventID: int
    EventType: str
    EventData: Dict
    EntityID: Optional[str]
    SourceID: int
    TransformerID: int
    Severity: Optional[str]
    Message: Optional[str]


class ProcessedEventCreate(ProcessedEventBase):
    pass


class ProcessedEventInDB(ProcessedEventBase):
    ProcessedEventID: int
    IngestionTime: datetime
    ProcessedTime: datetime
    Timestamp: datetime


class ProcessedEventResponse(ProcessedEventInDB):
    pass


class ProcessedEventFilter(BaseModel):
    event_types: Optional[List[str]] = None
    entity_ids: Optional[List[str]] = None
    source_ids: Optional[List[int]] = None
    start_timestamp: Optional[datetime] = None
    end_timestamp: Optional[datetime] = None
    severity: Optional[str] = None
    limit: int = 100
    offset: int = 0


class RelatedEntitiesEventsRequest(BaseModel):
    entity_id: str
    depth: int
    filter: ProcessedEventFilter


class ClusterEventFilter(BaseModel):
    components: Optional[List[int]] = None
    start_timestamp: Optional[datetime] = None
    end_timestamp: Optional[datetime] = None
    severities: Optional[List[str]] = None
    event_types: Optional[List[str]] = None


class ClusterEventsRequest(BaseModel):
    cluster_id: str
    last_event_id: Optional[int] = None
    page_size: int = 100
    offset: int = 0
    filters: ClusterEventFilter


class EventTypesResponse(BaseModel):
    event_type: str
    severity: str
