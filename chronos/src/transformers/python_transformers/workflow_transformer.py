import json
from loguru import logger

from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput
from src.transformers.base_python_transformer import BasePythonTransformer



class WorkflowEventsTransformer(BasePythonTransformer):
    async def is_transformer_applicable(self, event_data: RawEvent) -> bool:
        """
        Check if the transformer is applicable for the given event data.

        :param event_data: The raw event data as a string.
        :return: True if the transformer is applicable, False otherwise.
        """
        try:
            json.loads(event_data.EventData)
        except json.JSONDecodeError as e:
            logger.exception(f"Transformer is not applicable for event data: {event_data.EventData}: {e}")
            return None

        return True

    async def transform(self, event_data: RawEvent) -> TransformerOutput:
        """
        Transform the raw event data into processed events, links, and entities.

        :param event_data: The raw event data as a string.
        :return: A dictionary containing processed events, links, and entities.
        """
        try:
            raw_data = json.loads(event_data.EventData)
        except json.JSONDecodeError as e:
            logger.exception(f"Error in transforming data - {event_data}: {e}")
            return None

        processed_data = {
            "EventType": raw_data["state"],
            "EntityID": raw_data["entity_id"],
            "EventData": raw_data,
            "Message": raw_data["message"],
            "Severity": raw_data["severity"],
        }

        return TransformerOutput(
            processed_events=[processed_data], links=[], entities=[]
        )
