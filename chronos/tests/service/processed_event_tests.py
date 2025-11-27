import pytest
from unittest.mock import AsyncMock, patch
from src.service.processed_events import ProcessedEventsService
from src.models.models import ProcessedEvent
from src.dto.response.cluster_response import ClusterEventData


@pytest.mark.asyncio
async def test_get_cluster_events():
    # Mock the ProcessedEvent.filter method to return a predefined list of events
    mock_events = [
        ProcessedEvent(EventType="test_event", EntityID="test_cluster_id", ProcessedTime="2022-01-01T00:00:00Z",
                       Message="Test message", Severity="info", Source_id="1"),
    ]

    # Mock the find_related_entities method to return a predefined list of related entities
    mock_related_entities = ["1"]

    processed_events_service = ProcessedEventsService()

    with patch(ProcessedEvent, 'filter', return_value=AsyncMock()) as mock_filter:
        mock_filter.return_value.all.return_value = mock_events
        with patch.object(processed_events_service, 'find_related_entities',
                          return_value=AsyncMock()) as mock_find_related_entities:
            mock_find_related_entities.return_value = mock_related_entities
            cluster_events = await processed_events_service.get_cluster_events("test_cluster_id", None, None, 10, 0)

    # Check that the returned cluster events match the expected cluster events
    expected_cluster_events = [
        ClusterEventData(raw_event_id=None, processed_event_id=None, timestamp="2022-01-01T00:00:00Z",
                         event_type="test_event", message="Test message", severity="info", source_id="test_source_id"),
    ]
    assert cluster_events == expected_cluster_events
