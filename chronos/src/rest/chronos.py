from fastapi import APIRouter

from typing import Optional
from fastapi import APIRouter, Request, Header
from datetime import datetime
from typing import Optional
from src.service.EventsService import EventsService
from fastapi import APIRouter

from src.constant.constants import HEALTHCHECK_PATH, EVENTS_PATH, EVENT_PATH, SEARCH_EVENTS_PATH, RELATION_PATH, \
    RAW_EVENT_PATH
from src.dao.HealthcheckDao import health_check
from src.dto.BaseError import ServiceError
from src.dto.BaseResponse import BaseResponse
from src.dto.request.AddEventRequest import AddEventRequest
from src.dto.request.AddEventsRequest import AddEventsRequest
from src.dto.request.AddRelationRequest import AddRelationRequest


class Chronos:
    def __init__(self, event_service:EventsService):
        self.event_service = event_service
        self.router = APIRouter(prefix="/api/v1")
        self.router.add_api_route(HEALTHCHECK_PATH, self.health, methods=["GET"])
        self.router.add_api_route(EVENT_PATH, self.add_event, methods=["POST"])
        self.router.add_api_route(EVENTS_PATH, self.add_events, methods=["POST"])
        self.router.add_api_route(RAW_EVENT_PATH, self.add_raw_event, methods=["POST"])
        self.router.add_api_route(EVENT_PATH, self.get_event, methods=["GET"])
        self.router.add_api_route(SEARCH_EVENTS_PATH, self.search_event, methods=["GET"])
        self.router.add_api_route(RELATION_PATH, self.add_relation, methods=["POST"])
        self.router.add_api_route(RELATION_PATH, self.get_relation, methods=["GET"])
        self.router.add_api_route("/error", self.error, methods=["GET"])
        self.router.add_api_route("/resp", self.no_content_response, methods=["GET"])

    async def health(self):
        return await health_check()

    async def add_event(self, add_event_request: AddEventRequest):
        await self.events_service.add_event(add_event_request)
        return BaseResponse.get_no_content_response()

    async def add_events(self, add_events_request: AddEventsRequest):
        await self.events_service.add_events(add_events_request.events)
        return BaseResponse.get_no_content_response()

    async def add_raw_event(self, request: Request, source: str = Header(..., alias="x-event-source"),
                            timestamp: Optional[datetime] = Header(None, alias="x-event-timestamp")):
        if timestamp is None:
            timestamp = datetime.now()
        body = await request.body()
        await self.events_service.add_raw_event(details=body.decode('utf-8'), source=source, timestamp=timestamp)
        return BaseResponse.get_no_content_response()

    async def get_event(self, eventId: int):
        resp = await self.events_service.get_event(eventId)
        return BaseResponse(resp)

    async def search_event(self, startTime: datetime, endTime: datetime,
                           offset: int, pageSize: int = 20, eventType: Optional[str] = None,
                           entityId: Optional[str] = None):
        resp = await self.events_service.search_events(eventType, entityId, startTime, endTime, offset, pageSize)
        return BaseResponse(resp)

    async def add_relation(self, addRelationRequest: AddRelationRequest):
        res = await self.events_service.add_relation(addRelationRequest)
        return BaseResponse(res)

    async def get_relation(self, relationId: int):
        res = await self.events_service.get_relation(relationId)
        return BaseResponse(res)

    async def error(self):
        raise ServiceError("MY_ERROR", "my error", 501)

    async def no_content_response(self):
        return BaseResponse.get_no_content_response()


def get_chronos_router(events_service):
    return Chronos(events_service).router
