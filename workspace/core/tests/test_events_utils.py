import requests
from workspace_core.utils.events_utils import EventAPIClient
from unittest import TestCase
from unittest.mock import patch, MagicMock


class TestEventUtils(TestCase):
    def setUp(self):
        self.event_api_client = EventAPIClient("darwin-local")

    @patch("requests.post")
    def test_create_event_success(self, post_mock):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"test_key": "test_success"}

        # Configure the mock to return the mock response
        post_mock.return_value = mock_response

        resp = self.event_api_client.create_event({"test_key": "test_data"}, "source", "application/json")
        self.assertEqual(resp.status_code, 200)
        self.assertEqual(resp.json(), {"test_key": "test_success"})

    @patch("requests.post")
    def test_create_event_failure(self, post_mock):
        post_mock.side_effect = requests.exceptions.RequestException("Failed request")

        # Call create_event and assert that it doesn't raise the exception
        resp = self.event_api_client.create_event({"data": "data"}, "source", "application/json")

        # Verify that the response is None
        self.assertEqual(resp, None)
