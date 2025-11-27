import pytest
from unittest.mock import patch, AsyncMock

from src.dto.schema import JSONTransformerResponse, TransformerOutput, ProcessedEventData
from src.models.models import RawEvent, Source
from src.service.event_processor_service import EventProcessor


@pytest.mark.asyncio
async def test_process_event():
    raw_event = RawEvent(
        RawEventID=1,
        Source="test_source",
        ContentType="test_type",
        EventData=b'{"key": "value", "key2": "value2"}',
    )

    source_mock = AsyncMock(
        return_value=Source(
            SourceID=1,
            SourceName="test_source",
            EventFormatType="test_type",
            Description="Test source description"
        )
    )

    transformer_mock = AsyncMock(
        return_value=[
            JSONTransformerResponse(
                TransformerID=1,
                TransformerName="text",
                TransformerType="json",
                Description="Test transformer description",
                SourceID=1,
                ProcessType="async",
                MatchingEntity="value",
                MatchingPath="key",
                MatchingValue="value",
                EventMappings=[],
                EntityPath="key",
                EntityType="test_entity",
                EventType="test_event",
                Relations=[],
                Message="Test message",
                Severity="info"
            )
        ]
    )

    processed_event = TransformerOutput(
        processed_events=[
            ProcessedEventData(
                EventType="test_event",
                EntityID="value",
                EventData={"key": "value", "key2": "value2"},
                Severity="info",
                Message="Test message"
            )
        ],
        entities=[],
        links=[]
    )

    transformer_class_mock = AsyncMock()
    transformer_class_mock.apply = AsyncMock(return_value=processed_event)

    with patch('src.models.models.Source.get', source_mock):
        with patch('src.service.transformers.TransformerService.get_all_transformers_by_source',
                   transformer_mock):
            with patch('src.transformers.registry.transformer_registry.get_transformer',
                       return_value=transformer_class_mock):
                with patch(
                        'src.service.event_processor_service.EventProcessor._save_event_data') as save_event_data_mock:
                    with patch(
                            'src.service.event_processor_service.EventProcessor._save_entities') as save_entities_mock:
                        with patch('src.service.event_processor_service.EventProcessor._save_links') as save_links_mock:
                            await EventProcessor.process_event(raw_event)

                            source_mock.assert_called_once_with(SourceName="test_source", EventFormatType="test_type")
                            transformer_mock.assert_called_once_with(1)
                            transformer_class_mock.apply.assert_called_once_with(raw_event)
                            save_event_data_mock.assert_called_once_with(1, 1, 1, processed_event.processed_events)
                            save_entities_mock.assert_called_once_with(processed_event.entities)
                            save_links_mock.assert_called_once_with(processed_event.links)


@pytest.mark.asyncio
async def test_test_event():
    raw_event = RawEvent(
        RawEventID=1,
        Source="test_source",
        ContentType="test_type",
        EventData=b'{"key": "value", "key2": "value2"}',
    )

    source_mock = AsyncMock(
        return_value=Source(
            SourceID=1,
            SourceName="test_source",
            EventFormatType="test_type",
            Description="Test source description"
        )
    )

    transformer_mock = AsyncMock(
        return_value=[
            JSONTransformerResponse(
                TransformerID=1,
                TransformerName="text",
                TransformerType="json",
                Description="Test transformer description",
                SourceID=1,
                ProcessType="async",
                MatchingEntity="value",
                MatchingPath="key",
                MatchingValue="value",
                EventMappings=[],
                EntityPath="key",
                EntityType="test_entity",
                EventType="test_event",
                Relations=[],
                Message="Test message",
                Severity="info"
            )
        ]
    )

    processed_event = TransformerOutput(
        processed_events=[
            ProcessedEventData(
                EventType="test_event",
                EntityID="value",
                EventData={"key": "value", "key2": "value2"},
                Severity="info",
                Message="Test message"
            )
        ],
        entities=[],
        links=[]
    )

    transformer_class_mock = AsyncMock()
    transformer_class_mock.apply = AsyncMock(return_value=processed_event)

    with patch('src.models.models.Source.get', source_mock):
        with patch('src.service.transformers.TransformerService.get_all_transformers_by_source',
                   transformer_mock):
            with patch('src.transformers.registry.transformer_registry.get_transformer',
                       return_value=transformer_class_mock):
                await EventProcessor.process_event(raw_event)

                source_mock.assert_called_once_with(SourceName="test_source", EventFormatType="test_type")
                transformer_mock.assert_called_once_with(1)
                transformer_class_mock.apply.assert_called_once_with(raw_event)
