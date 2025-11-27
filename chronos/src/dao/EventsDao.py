import json
from datetime import datetime

from src.client.mysql_client import MysqlClient
from src.constant.queries import ADD_EVENT_QUERY, GET_EVENT_QUERY, get_search_events_query, ADD_RELATION_QUERY, \
    GET_RELATION_BY_FROM_ID_QUERY, GET_RELATION_QUERY, ADD_RAW_EVENT_QUERY
from src.dto.BaseError import ServiceError, EVENT_NOT_FOUND_EXCEPTION_CODE
from src.dto.request.AddEventRequest import AddEventRequest
from src.dto.response.GetEventResponse import GetEventResponse
from src.dto.response.SearchEventsResponse import SearchEventsResponse


class EventsDao:
    mysql_client: MysqlClient

    def __init__(self, mysql_client: MysqlClient):
        self.mysql_client = mysql_client

    async def add_events(self, events: list[AddEventRequest]):
        li = []
        for event in events:
            li.append((event.source, event.entity_id, event.event_type, event.timestamp, json.dumps(event.details)))
        return await self.mysql_client.execute_bulk_query(query=ADD_EVENT_QUERY, params_list=li)

    async def add_event(self, event: AddEventRequest):
        return await self.mysql_client.execute_query(query=ADD_EVENT_QUERY, params=(
            event.source, event.entity_id, event.event_type, event.timestamp, json.dumps(event.details)),
                                                     get_last_insert_id=True)

    async def add_raw_event(self, source: str, timestamp: datetime, details: str):
        return await self.mysql_client.execute_query(query=ADD_RAW_EVENT_QUERY, params=(
            source, timestamp, details), get_last_insert_id=True, raw_res=True)

    async def get_event(self, event_id: int):
        res = await self.mysql_client.execute_query(query=GET_EVENT_QUERY, params=(event_id,),
                                                    is_select_query=True)
        return res

    async def search_events(self, eventType: str, entityId: str, startTime: datetime, endTime: datetime,
                            pageSize: int, offset: int):
        query, params = get_search_events_query(eventType, entityId, startTime, endTime, pageSize, offset)
        res = await self.mysql_client.execute_query(query=query, params=params,
                                                    is_select_query=True)

        if len(res) == 0:
            return SearchEventsResponse([], False)
        events = [GetEventResponse(**e) for e in res]
        return SearchEventsResponse(events, False if len(events) < pageSize else True)

    async def get_relation_from_id(self, from_entity_id: str):
        return await self.mysql_client.execute_query(query=GET_RELATION_BY_FROM_ID_QUERY, params=(from_entity_id,),
                                                     is_select_query=True)

    async def get_relation(self, relation_id: int):
        return await self.mysql_client.execute_query(query=GET_RELATION_QUERY, params=(relation_id,),
                                                     is_select_query=True)

    async def add_relation(self, from_entity_id: str, to_entity_id: str):
        return await self.mysql_client.execute_query(query=ADD_RELATION_QUERY, params=(from_entity_id, to_entity_id),
                                                     get_last_insert_id=True)
