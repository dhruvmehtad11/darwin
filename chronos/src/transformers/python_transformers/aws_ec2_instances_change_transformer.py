import json
from loguru import logger

from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput
from src.transformers.base_python_transformer import BasePythonTransformer
from src.transformers.python_transformers.enums.events import ComputeEvent, AwsEvent



class AwsEc2InstanceChangeEventTransformer(BasePythonTransformer):

    async def is_transformer_applicable(self, event_data: RawEvent) -> bool:
        """
            Check if the transformer is applicable for the given event data.
            :param event_data: The raw event data as a string.
            :return: True if the transformer is applicable, False otherwise.
            """
        try:
            raw_data = json.loads(event_data.EventData)
        except json.JSONDecodeError as e:
            logger.exception(f"Error in applying transformer for data - {event_data} : {e}")
            return False
        if 'detail-type' in raw_data and raw_data['detail-type'] == 'EC2 Instance State-change Notification':
            return True

        return False

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

        instance_state = raw_data['detail']['state']
        instance_id = raw_data['detail']['instance-id']
        aws_event_id = raw_data['id']
        event_type = AwsEvent.get_event_type(instance_state)
        severity = event_type.get_severity()

        processed_data = {
            'EventType': event_type.name,
            'EntityID': instance_id,
            'EventData': {
                'instanceState': instance_state,
                'awsEventID': aws_event_id
            },
            'Severity': severity,
            'Message': f'Instance with {instance_id} state changed to {instance_state}'
        }

        return TransformerOutput(
            processed_events=[processed_data],
            entities=[],
            links=[]
        )
