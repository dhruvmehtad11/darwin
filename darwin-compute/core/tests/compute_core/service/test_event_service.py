import json
import pytest
import unittest
from unittest.mock import patch, Mock

from compute_core.constant.constants import DEFAULT_CLUSTER_EVENTS
from compute_core.service.event_service import EventService
from compute_core.dto.chronos_dto import ChronosEvent
from compute_core.util.utils import default_events


class TestEventService(unittest.IsolatedAsyncioTestCase):
    @pytest.fixture(autouse=True)
    def prepare_fixture(self, all_cluster_events):
        self.all_cluster_events = all_cluster_events

    def test_send_event(self):
        event_service = EventService()
        event_data = ChronosEvent(
            event_type="test_event",
            cluster_id="test_cluster",
            message="This is a test event",
            timestamp=1234567890,
        )
        with patch("compute_core.service.event_service.make_api_request") as mock_request:
            mock_response = Mock()
            mock_response.json.return_value = {"status": "success"}
            mock_request.return_value = mock_response

            response = event_service.send_event(event_data)

            mock_request.assert_called_once_with(
                method="POST",
                url=f"{event_service.config.get_chronos_url}/api/v1/event",
                data=event_data.to_dict(encode_json=True),
                headers=event_service.headers,
                max_retries=3,
                timeout=2,
            )

    @patch("compute_core.service.event_service.EventService._request")
    async def test_get_default_events_success(self, mock_request):
        expected = default_events(self.all_cluster_events, DEFAULT_CLUSTER_EVENTS)
        mock_request.return_value = {"data": self.all_cluster_events}
        event_service = EventService()
        resp = await event_service.get_default_events()
        resp = json.loads(resp.body)
        self.assertEqual(expected, resp["data"])

    @patch("compute_core.service.event_service.EventService._request")
    async def test_get_default_events_exception(self, mock_request):
        expected_error = "ERROR"
        expected_message = "Error getting default events"
        mock_request.side_effect = Exception(expected_error)
        event_service = EventService()
        resp = await event_service.get_default_events()
        resp = json.loads(resp.body)
        self.assertEqual(expected_error, resp["status"])
        self.assertEqual(expected_message, resp["message"])
