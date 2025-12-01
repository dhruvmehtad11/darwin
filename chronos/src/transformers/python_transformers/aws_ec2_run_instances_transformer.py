import json
from loguru import logger

from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput
from src.transformers.base_python_transformer import BasePythonTransformer
from src.transformers.python_transformers.enums.events import ComputeEvent, AwsEvent



class AwsEc2RunInstanceTransformer(BasePythonTransformer):

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
        if 'eventName' in raw_data and raw_data['eventName'] == 'RunInstances':
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

        launch_time = raw_data['launchTime']
        instance_id = raw_data['instanceId']
        host_ip = raw_data['privateDnsName']
        instance_capacity_type = raw_data['instanceLifecycle']
        iam_instance_profile = raw_data['iamInstanceProfile']
        aws_event_id = raw_data['eventID']
        event_type = AwsEvent.INSTANCE_LAUNCHED
        severity = event_type.get_severity()
        instance_type = raw_data['instanceType']

        processed_data = {
            'EventType': event_type.name,
            'EntityID': instance_id,
            'EventData': {
                'launchTime': launch_time,
                'hostIP': host_ip,
                'instanceType': instance_type,
                'iamInstanceProfile': iam_instance_profile,
                'awsEventID': aws_event_id,
                'instanceCapacityType': instance_capacity_type

            },
            'Severity': severity,
            'Message': f"Instance {instance_id} launched of {instance_type} instance type and capacity type {instance_capacity_type}"
        }

        links = [
            {
                'SourceEntityID': instance_id,
                'DestinationEntityID': host_ip
            }
        ]

        entities = [
            {
                'EntityID': instance_id,
                'EntityType': 'Host'
            },
            {
                'EntityID': host_ip,
                'EntityType': 'HostIP'
            }
        ]

        return TransformerOutput(
            processed_events=[processed_data],
            entities=entities,
            links=links
        )
