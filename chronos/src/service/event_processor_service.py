from typing import List, Optional

from src.consumers.configs.config import Config
from src.dao.stream_writer_factory import get_stream_writer
from src.dto.schema import LinkCreate, TestEventResponse
from src.dto.schema.base_transformers import ProcessedEventData, EntityData, RelationData
from src.errors.errors import SourceNotFound, TransformersNotFound
from src.models.models import RawEvent, Source, Entity, ProcessedEvent, Link, ProcessedEventV2, EventType
from src.service.transformers import TransformerService
from src.transformers.registry import transformer_registry
import tortoise.exceptions
from loguru import logger

transformer_service = TransformerService()


class EventProcessor:

    def __init__(self, env):
        self.config = Config(env)
        self.writer = get_stream_writer(env)

    async def process_event(self, raw_event: RawEvent):
        # Check the source table
        source = await Source.get(SourceName=raw_event.Source, EventFormatType=raw_event.ContentType)

        # Get applicable transformers
        transformers = await transformer_service.get_all_transformers_by_source(source.SourceID)

        for transformer in transformers:
            try:
                transformer_class = await transformer_registry.get_transformer(transformer)
                processed_event_data = await transformer_class.apply(raw_event)

                if processed_event_data is None:
                    continue
                event_types = [
                    {
                        "event_type": event.EventType,
                        "severity": event.Severity
                    } for event in processed_event_data.processed_events
                ]

                await self._save_event_data(
                    source.SourceID,
                    raw_event.RawEventID,
                    transformer.TransformerID,
                    processed_event_data.processed_events)
                await self._save_event_types(event_types)
                await self._save_entities(processed_event_data.entities)
                await self._save_links(processed_event_data.links)
                # TODO mark the event as isProcessed as true
            except Exception as e:
                # TODO mark the event as isProcessed as False or log the error
                logger.exception(f"Error processing event {raw_event.RawEventID} with transformer {transformer.TransformerID} - {e}")

    async def _save_event_types(self, event_types: List[EventType]):
        if (event_types is None) or (len(event_types) == 0):
            return
        for event_type in event_types:
            try:
                await EventType.get_or_create(**event_type)
            except Exception as e:
                logger.exception(f"Error saving event types - {event_types} : {e}")

    async def _save_event_data(self, source_id: int, raw_event_id: int, transformer_id: int,
                               processed_data: Optional[List[ProcessedEventData]]):
        if (processed_data is None) or (len(processed_data) == 0):
            return

        for processed_event_data in processed_data:
            try:
                await ProcessedEventV2.create(
                    RawEventID=raw_event_id,
                    EventType=processed_event_data.EventType,
                    EventData=processed_event_data.EventData,
                    EntityID=processed_event_data.EntityID,
                    Source_id=source_id,
                    Transformer_id=transformer_id,
                    Severity=processed_event_data.Severity,
                    Message=processed_event_data.Message
                )

                processed_event = await ProcessedEvent.create(
                    RawEventID=raw_event_id,
                    EventType=processed_event_data.EventType,
                    EventData=processed_event_data.EventData,
                    EntityID=processed_event_data.EntityID,
                    Source_id=source_id,
                    Transformer_id=transformer_id,
                    Severity=processed_event_data.Severity,
                    Message=processed_event_data.Message
                )
                try:
                    self.writer.write_record(self.config.get_processed_event_destination, processed_event.to_dict())
                except Exception as e:
                    logger.exception(f"Failed to write processed event {processed_event.ProcessedEventID} to queue: {e}")
            except Exception as e:
                logger.exception(f"Failed to save event data : {e}")

    async def _save_entities(self, entities: Optional[List[EntityData]]):
        if (entities is None) or (len(entities) == 0):
            return
        for entity_data in entities:
            try:
                if entity_data is None:
                    continue
                try:
                    # First try to get the entity
                    await Entity.get(EntityID=entity_data.EntityID)
                except tortoise.exceptions.DoesNotExist:
                    # If it doesn't exist, try to create it
                    await Entity.create(**entity_data.dict())
            except Exception as e:
                logger.exception(f"Failed to save entities: {e}")

    async def _save_links(self, links: Optional[List[RelationData]]):
        if (links is None) or (len(links) == 0):
            return
        for link_data in links:
            src = link_data.SourceEntityID
            destination = link_data.DestinationEntityID
            if src != destination:
                try:
                    right = LinkCreate(**link_data.dict())
                    left = LinkCreate(DestinationEntityID=src, SourceEntityID=destination)
                    await Link.get_or_create(**right.dict())
                    await Link.get_or_create(**left.dict())
                except Exception as e:
                    logger.exception(f"Failed to save links: {e}")

    async def test_event(self, raw_event: RawEvent) -> List[TestEventResponse]:
        # Check the source table
        source = await Source.get(SourceName=raw_event.Source, EventFormatType=raw_event.ContentType)

        if (source is None):
            raise SourceNotFound("Source not found for the given message")

        # Get applicable transformers
        transformers = await transformer_service.get_all_transformers_by_source(source.SourceID)

        if (transformers is None or len(transformers) == 0):
            raise TransformersNotFound("No transformers found for the given source")

        processed_event_response_list: List[TestEventResponse] = []

        for transformer in transformers:
            try:
                transformer_class = await transformer_registry.get_transformer(transformer)
                processed_event_data = await transformer_class.apply(raw_event)
                processed_event_response_list.append(TestEventResponse(
                    source_id=source.SourceID,
                    transformer_id=transformer.TransformerID,
                    processed_data=processed_event_data,
                    status="Success",
                    message="Successfully processed the event"

                ))
            except Exception as e:
                logger.exception(f"Failed to process the event : {e}")
                processed_event_response_list.append(TestEventResponse(
                    source_id=source.SourceID,
                    transformer_id=transformer.TransformerID,
                    processed_data=None,
                    status="Failed",
                    message=f"Failed to process the event - {e}"
                ))
        return processed_event_response_list
