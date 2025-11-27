import datetime
from loguru import logger
from typing import List, Set, Optional

from tortoise.transactions import atomic
from tortoise.queryset import QuerySet

from src.constant.constants import CLUSTER_EVENTS_DEPTH
from src.dto.response.cluster_response import ClusterSession, ClusterEventData
from src.dto.schema.processed_event import ProcessedEventCreate, ProcessedEventResponse, ProcessedEventFilter, \
    ClusterEventFilter, EventTypesResponse
from src.models.models import ProcessedEvent, Link, EventType
from src.transformers.python_transformers.enums.events import ComputeEvent, PodEvent
from src.util.event_utils import get_ui_events


class ProcessedEventsService:
    @atomic("default")
    async def create_processed_event(self, event: ProcessedEventCreate) -> ProcessedEventResponse:
        event_obj = await ProcessedEvent.create(
            RawEventID=event.RawEventID,
            EventType=event.EventType,
            EventData=event.EventData,
            EntityID=event.EntityID,
            Source_id=event.SourceID,
            Transformer_id=event.TransformerID
        )
        return ProcessedEventResponse(
            ProcessedEventID=event_obj.ProcessedEventID,
            RawEventID=event_obj.RawEventID,
            EventType=event_obj.EventType,
            EventData=event_obj.EventData,
            EntityID=event_obj.EntityID,
            SourceID=event_obj.Source_id,
            TransformerID=event_obj.Transformer_id,
            IngestionTime=event_obj.IngestionTime.isoformat(),
            ProcessedTime=event_obj.ProcessedTime.isoformat(),
            Timestamp=event.ProcessedTime.isoformat(),
            Message=event_obj.Message,
            Severity=event_obj.Severity
        )

    async def get_processed_event(self, event_id: int) -> ProcessedEventResponse:
        event = await ProcessedEvent.get(ProcessedEventID=event_id)
        return ProcessedEventResponse(
            ProcessedEventID=event.ProcessedEventID,
            RawEventID=event.RawEventID,
            EventType=event.EventType,
            EventData=event.EventData,
            EntityID=event.EntityID,
            SourceID=event.Source_id,
            TransformerID=event.Transformer_id,
            IngestionTime=event.IngestionTime.isoformat(),
            ProcessedTime=event.ProcessedTime.isoformat(),
            Timestamp=event.ProcessedTime.isoformat(),
            Message=event.Message,
            Severity=event.Severity
        )

    async def get_all_processed_events(self, limit: int = 100) -> List[ProcessedEventResponse]:
        events = await ProcessedEvent.all().limit(limit)
        return [
            ProcessedEventResponse(
                ProcessedEventID=event.ProcessedEventID,
                RawEventID=event.RawEventID,
                EventType=event.EventType,
                EventData=event.EventData,
                EntityID=event.EntityID,
                SourceID=event.Source_id,
                TransformerID=event.Transformer_id,
                IngestionTime=event.IngestionTime.isoformat(),
                ProcessedTime=event.ProcessedTime.isoformat(),
                Timestamp=event.ProcessedTime.isoformat(),
                Message=event.Message,
                Severity=event.Severity
            )
            for event in events
        ]

    async def delete_processed_event(self, event_id: int) -> ProcessedEventResponse:
        event = await ProcessedEvent.get(ProcessedEventID=event_id)
        await event.delete()
        return ProcessedEventResponse(
            ProcessedEventID=event.ProcessedEventID,
            RawEventID=event.RawEventID,
            EventType=event.EventType,
            EventData=event.EventData,
            EntityID=event.EntityID,
            SourceID=event.Source_id,
            TransformerID=event.Transformer_id,
            IngestionTime=event.IngestionTime.isoformat(),
            ProcessedTime=event.ProcessedTime.isoformat(),
            Timestamp=event.ProcessedTime.isoformat(),
            Message=event.Message,
            Severity=event.Severity
        )

    async def get_processed_events(self, filter_request: ProcessedEventFilter):
        query = ProcessedEvent.all()

        if filter_request.event_types and len(filter_request.event_types) > 0:
            query = query.filter(EventType__in=filter_request.event_types)
        if filter_request.entity_ids and len(filter_request.entity_ids) > 0:
            query = query.filter(EntityID__in=filter_request.entity_ids)
        if filter_request.severity:
            query = query.filter(Severity=filter_request.severity)
        if filter_request.source_ids:
            query = query.filter(Source_id__in=filter_request.source_ids)
        if filter_request.start_timestamp:
            query = query.filter(ProcessedTime__gte=filter_request.start_timestamp)
        if filter_request.end_timestamp:
            query = query.filter(ProcessedTime__lte=filter_request.end_timestamp)

        query = query.order_by("ProcessedTime").limit(filter_request.limit).offset(filter_request.offset)

        processed_events = await query

        return [
            ProcessedEventResponse(
                ProcessedEventID=event.ProcessedEventID,
                RawEventID=event.RawEventID,
                EventType=event.EventType,
                EventData=event.EventData,
                EntityID=event.EntityID,
                SourceID=event.Source_id,
                TransformerID=event.Transformer_id,
                IngestionTime=event.IngestionTime.isoformat(),
                ProcessedTime=event.ProcessedTime.isoformat(),
                Timestamp=event.ProcessedTime.isoformat(),
                Message=event.Message,
                Severity=event.Severity
            )
            for event in processed_events
        ]

    async def find_related_entities(self, entity_id: str, depth: int) -> Set[str]:
        related_entities = set()
        current_level_entities = {entity_id}
        next_level_entities = set()

        for _ in range(depth):
            if not current_level_entities:
                break

            source_links = await Link.filter(SourceEntityID__in=current_level_entities)
            for link in source_links:
                next_level_entities.add(link.DestinationEntityID)

            related_entities = related_entities.union(current_level_entities)
            current_level_entities = next_level_entities
            next_level_entities = set()

        related_entities = related_entities.union(current_level_entities)
        return related_entities

    async def get_events_for_entities(self, entity_ids: List[str], filter_params: ProcessedEventFilter) -> List[
        ProcessedEventResponse]:
        query = ProcessedEvent.filter(EntityID__in=entity_ids)

        query = self.__filter_processed_events(query, filter_params)

        processed_events = await query

        return [
            ProcessedEventResponse(
                ProcessedEventID=event.ProcessedEventID,
                RawEventID=event.RawEventID,
                EventType=event.EventType,
                EventData=event.EventData,
                EntityID=event.EntityID,
                SourceID=event.Source_id,
                TransformerID=event.Transformer_id,
                IngestionTime=event.IngestionTime.isoformat(),
                ProcessedTime=event.ProcessedTime.isoformat(),
                Timestamp=event.ProcessedTime.isoformat(),
                Message=event.Message,
                Severity=event.Severity
            )
            for event in processed_events
        ]

    async def get_cluster_events(self, cluster_id: str, last_event_id: Optional[int],
                                 filter_params: ClusterEventFilter, limit: int, offset: int) \
            -> List[ClusterEventData]:
        current_time = datetime.datetime.now()
        related_entities = await self.find_related_entities(
            cluster_id, CLUSTER_EVENTS_DEPTH
        )
        logger.info(f"Found {len(related_entities)} related entities for cluster with id {cluster_id} in {datetime.datetime.now() - current_time}")

        current_time = datetime.datetime.now()
        query = ProcessedEvent.filter(EntityID__in=related_entities)

        if last_event_id:
            query = query.filter(ProcessedEventID__gt=last_event_id)

        if filter_params.severities:
            query = query.filter(Severity__in=filter_params.severities)
        if filter_params.components:
            query = query.filter(Source_id__in=filter_params.components)
        if filter_params.start_timestamp:
            query = query.filter(ProcessedTime__gte=filter_params.start_timestamp)
        if filter_params.end_timestamp:
            query = query.filter(ProcessedTime__lte=filter_params.end_timestamp)

        allowed_events = get_ui_events()

        if filter_params.event_types:
            # Taking intersection of allowed_events and filter_params.event_types
            event_list = list(set(filter_params.event_types) & set(allowed_events))
            query = query.filter(EventType__in=event_list)
        else:
            query = query.filter(EventType__in=allowed_events)

        query = query.order_by("-ProcessedTime").limit(limit).offset(offset)

        processed_events = await query
        logger.info(f"Found cluster events for cluster with id {cluster_id} in {datetime.datetime.now() - current_time}")

        return [
            ClusterEventData(
                raw_event_id=event.RawEventID,
                processed_event_id=event.ProcessedEventID,
                timestamp=event.ProcessedTime,
                event_type=event.EventType,
                message=event.Message,
                severity=event.Severity if event.Severity else "INFO",
                source_id=event.Source_id
            )
            for event in processed_events
        ]

    def __filter_processed_events(self, query: QuerySet[ProcessedEvent], filter_params: ProcessedEventFilter):
        if filter_params.event_types and len(filter_params.event_types) > 0:
            query = query.filter(EventType__in=filter_params.event_types)
        if filter_params.severity:
            query = query.filter(Severity=filter_params.severity)
        if filter_params.source_ids:
            query = query.filter(Source_id__in=filter_params.source_ids)
        if filter_params.start_timestamp:
            query = query.filter(ProcessedTime__gte=filter_params.start_timestamp)
        if filter_params.end_timestamp:
            query = query.filter(ProcessedTime__lte=filter_params.end_timestamp)

        query = query.order_by("-ProcessedTime").filter().limit(filter_params.limit).offset(filter_params.offset)

        return query

    async def get_cluster_sessions(self, cluster_id: str):
        # Fetch all the start and stop events for the given cluster_id and within the given timestamps
        events = await ProcessedEvent.filter(EntityID=cluster_id).filter(
            EventType__in=[ComputeEvent.CLUSTER_START_REQUEST_RECEIVED.name, ComputeEvent.CLUSTER_STOPPED.name]
        ).order_by("-ProcessedTime").all()

        start_events = [event for event in events if
                        event.EventType == ComputeEvent.CLUSTER_START_REQUEST_RECEIVED.name]
        stop_events = [event for event in events if event.EventType == ComputeEvent.CLUSTER_STOPPED.name]

        start_events.sort(key=lambda x: x.ProcessedTime, reverse=True)
        stop_events.sort(key=lambda x: x.ProcessedTime, reverse=True)

        # Initialize an empty list to store the sessions
        sessions: List[ClusterSession] = []

        # Iterate over the start events
        for start_event in start_events:
            # Find the corresponding stop event with the same session id
            stop_event = next((event for event in stop_events if
                               event.EventData["session_id"] == start_event.EventData["session_id"]), None)
            if stop_event:
                end_time = stop_event.ProcessedTime
            else:
                end_time = None

            # If a matching stop event is found, create a GetSessionResponse object and add it to the sessions list
            session = ClusterSession(
                session_id=start_event.EventData["session_id"],
                start_timestamp=start_event.ProcessedTime,
                end_timestamp=end_time
            )
            sessions.append(session)

        # Return the sessions list
        return sessions

    async def get_event_types(self):
        event_types = await EventType.all()
        return [
            EventTypesResponse(
                event_type=event_type.event_type,
                severity=event_type.severity
            )
            for event_type in event_types
        ]