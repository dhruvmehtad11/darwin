from datetime import datetime

from src.client.mysql_client import MysqlClient
from src.dao.EventsDao import EventsDao
from src.dto.BaseError import ServiceError, EVENT_NOT_FOUND_EXCEPTION_CODE, EVENT_SEARCH_EXCEPTION_CODE, \
    ENTITY_RELATION_EXCEPTION_CODE, ENTITY_RELATION_NOT_FOUND_EXCEPTION_CODE, \
    ENTITY_RELATION_ALREADY_EXISTS_EXCEPTION_CODE
from src.dto.request.AddEventRequest import AddEventRequest
from src.dto.request.AddRelationRequest import AddRelationRequest
from src.dto.response.AddRelationResponse import AddRelationResponse
from src.dto.response.GetEventResponse import GetEventResponse
from src.dto.response.GetRelationResponse import GetRelationResponse
from src.dto.response.SearchEventsResponse import SearchEventsResponse


class EventsService:
    events_dao: EventsDao

    def __init__(self, mysql_client: MysqlClient):
        self.events_dao = EventsDao(mysql_client)

    async def add_events(self, events: list[AddEventRequest]):
        return await self.events_dao.add_events(events)

    async def add_event(self, event: AddEventRequest):
        return await self.events_dao.add_event(event)

    async def add_raw_event(self, details: str, source: str, timestamp: datetime):
        return await self.events_dao.add_raw_event(source=source, timestamp=timestamp, details=details)

    async def get_event(self, event_id: int):
        res = await self.events_dao.get_event(event_id)
        if len(res) == 0:
            raise ServiceError(EVENT_NOT_FOUND_EXCEPTION_CODE, "event not found", 400)
        event = res[0]
        return GetEventResponse(**event)

    async def get_events(self, event_id: int):
        event = await self.events_dao.get_event(event_id)
        return GetEventResponse(**event)

    async def search_events(self, eventType: str, entityId: str, startTime: datetime, endTime: datetime,
                            pageSize: int, offset: int):
        if eventType is None and entityId is None:
            raise ServiceError(EVENT_SEARCH_EXCEPTION_CODE, "both eventType and entityId cannot be null at once", 400)

        if startTime >= endTime:
            raise ServiceError(EVENT_SEARCH_EXCEPTION_CODE, "startTime cannot be greater than endTime", 400)

        return await self.events_dao.search_events(eventType, entityId, startTime, endTime, offset, pageSize)

    async def add_relation(self, addRelationRequest: AddRelationRequest):
        if addRelationRequest.from_entity_id == addRelationRequest.to_entity_id:
            raise ServiceError(ENTITY_RELATION_EXCEPTION_CODE, "fromEntityId and toEntityId cannot be the same", 400)
        
        id = await self.events_dao.add_relation(addRelationRequest.from_entity_id, addRelationRequest.to_entity_id)
        return AddRelationResponse(id)

    async def get_relation(self, relationId: int):
        res = await self.events_dao.get_relation(relationId)
        if len(res) == 0:
            raise ServiceError(ENTITY_RELATION_NOT_FOUND_EXCEPTION_CODE, "entity relation not found", 400)

        relation = res[0]
        return GetRelationResponse(**relation)
