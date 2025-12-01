from datetime import datetime

import requests

import workspace_core.utils.logging_util as logging
from workspace_core.constants.config import Config
from workspace_core.constants.constants import DATE_FORMAT

logger = logging.get_logger(__name__)


class BaseSingleton(type):
    """
    A Singleton metaclass.
    Ensures only one instance of the class can be created.
    """

    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            instance = super().__call__(*args, **kwargs)
            cls._instances[cls] = instance
        return cls._instances[cls]


class EventAPIClient(metaclass=BaseSingleton):
    def __init__(self, env: str):
        self.env = env
        self.base_url = Config(env).chronos_url
        self.source = "WORKSPACE_APP_LAYER"

    def create_event(self, event_data: dict, source: str, content_type: str = "application/json"):
        try:
            event_timestamp = datetime.now().strftime(DATE_FORMAT)

            if source is None:
                source = self.source

            headers = {"x-event-source": source, "x-event-timestamp": event_timestamp, "Content-Type": content_type}
            logger.info(f"debugging data = : {event_data}")
            response = requests.post(f"{self.base_url}/api/v1/event", headers=headers, json=event_data)
            response.raise_for_status()
            return response
        except Exception as e:
            logger.error(f"events.post.failed.error_messages: {e}")
