from typing import List

from fastapi import APIRouter, HTTPException
from loguru import logger
from src.constant.constants import CLUSTER_EVENTS_DEPTH
from src.dto.response.cluster_response import ClusterProcessedEvent, GetProcessedEventResponse
from src.dto.schema.processed_event import (
    ProcessedEventResponse, ProcessedEventCreate, ProcessedEventFilter,
    RelatedEntitiesEventsRequest, ClusterEventsRequest, EventTypesResponse
)
from src.service.processed_events import ProcessedEventsService


class ProcessedEventRouter:
    def __init__(self):
        self.router = APIRouter()
        self.processed_event_service = ProcessedEventsService()
        self.register_routes()

    def register_routes(self):
        self.router.post("/processed_events/", response_model=ProcessedEventResponse)(self.create_processed_event)
        self.router.get("/processed_events/{event_id}", response_model=GetProcessedEventResponse)(
            self.read_processed_event)
        self.router.delete("/processed_events/{event_id}", response_model=ProcessedEventResponse)(
            self.delete_processed_event)
        self.router.get("/processed_events/", response_model=List[ProcessedEventResponse])(self.read_processed_events)
        self.router.post("/processed_events/filter", response_model=List[ProcessedEventResponse])(
            self.get_processed_events)
        self.router.post("/related_entities_events", response_model=List[ProcessedEventResponse])(
            self.get_related_entities_events)
        self.router.get("/processed_events/event_types", response_model=List[EventTypesResponse])(
            self.get_event_types)

    async def get_event_types(self):
        return await self.processed_event_service.get_event_types()

    async def create_processed_event(self, event: ProcessedEventCreate):
        return await self.processed_event_service.create_processed_event(event)

    async def read_processed_event(self, event_id: int):
        event = await self.processed_event_service.get_processed_event(event_id)
        if not event:
            raise HTTPException(status_code=404, detail="Processed Event not found")
        return GetProcessedEventResponse(
            data=ClusterProcessedEvent(
                processed_event_id=event.ProcessedEventID,
                event_data=event,
                timestamp=event.ProcessedTime
            )
        )

    async def delete_processed_event(self, event_id: int):
        return await self.processed_event_service.delete_processed_event(event_id)

    async def read_processed_events(self):
        return await self.processed_event_service.get_all_processed_events()

    async def get_processed_events(self, filter_request: ProcessedEventFilter):
        try:
            return await self.processed_event_service.get_processed_events(filter_request)
        except Exception as e:
            logger.exception(f"Error getting processed events: {e}")
            raise HTTPException(status_code=500, detail=str(e))

    async def get_related_entities_events(self, request: RelatedEntitiesEventsRequest):
        related_entities = await self.processed_event_service.find_related_entities(request.entity_id, request.depth)
        if not related_entities:
            raise HTTPException(status_code=404, detail="No related entities found")

        processed_events = await self.processed_event_service.get_events_for_entities(list(related_entities),
                                                                                      request.filter)
        return processed_events


processed_event_router = ProcessedEventRouter().router
