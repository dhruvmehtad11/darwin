from typing import List, Dict, Any, Optional

from pydantic import BaseModel


class ProcessedEventData(BaseModel):
    EventType: str
    EntityID: str
    EventData: Optional[Dict[str, Any]]
    Severity: Optional[str] = None
    Message: Optional[str] = None


class EntityData(BaseModel):
    EntityID: str
    EntityType: str


class RelationData(BaseModel):
    SourceEntityID: str
    DestinationEntityID: str


class TransformerOutput(BaseModel):
    processed_events: Optional[List[ProcessedEventData]]
    entities: Optional[List[EntityData]]
    links: Optional[List[RelationData]]
