import json
from loguru import logger

from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput
from src.service.links import LinkService
from src.transformers.base_python_transformer import BasePythonTransformer
from src.transformers.python_transformers.enums.events import PodEvent
from src.transformers.utils.transformer_utils import get_base_cluster_id, get_instance_id_from_ip, \
    convert_private_dns_name_to_ip, get_instance_id

class KubernetesPodEventsTransformer(BasePythonTransformer):

    def __init__(self):
        self.link_service = LinkService()

    async def is_transformer_applicable(self, event_data: RawEvent) -> bool:
        """
        Check if the transformer is applicable for the given event data.

        :param event_data: The raw event data as a string.
        :return: True if the transformer is applicable, False otherwise.
        """
        # Check if the event has involvedObject field
        try:
            event_data = json.loads(event_data.EventData)
        except json.JSONDecodeError as e:
            logger.exception(f"Error in applying transformer for data - {event_data} : {e}")
            return None

        if 'involvedObject' not in event_data:
            return False

        # Check if the event is related to a pod
        if event_data['involvedObject']['kind'] != 'Pod':
            return False

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

        cluster_id = get_base_cluster_id(raw_data['involvedObject']['name'], raw_data['involvedObject']['namespace'])
        event_type = PodEvent.get_event_type(raw_data['reason'])
        severity = event_type.get_severity()

        if 'source' in raw_data and 'host' in raw_data['source']:
            host_ip = raw_data['source']['host']
        else:
            host_ip = None

        try:
            if event_type == PodEvent.POD_SCHEDULED:
                host_ip = raw_data['message'].split()[-1]
        except Exception as e:
            logger.error(f"Error while getting host ip from message. The error is {e}")
            host_ip = None

        if host_ip and (event_type == PodEvent.IMAGE_PULLED or event_type == PodEvent.IMAGE_PULLING or event_type == PodEvent.POD_SCHEDULED):
            try:
                logger.info(f"Getting instance id for ip {host_ip}")
                ip = convert_private_dns_name_to_ip(host_ip)
                host_id = get_instance_id(ip)
            except Exception as e:
                logger.exception(
                    f"Error while converting private DNS name to instance id for ip {host_ip}. The error is {e}")
                host_id = None
        else:
            host_id = None

        processed_data = {
            'EventType': event_type.name,
            'EntityID': raw_data['involvedObject']['name'],
            'EventData': {
                'cluster_id': cluster_id,
                'kubernetes_event_id': raw_data['metadata']['uid'],
                'first_occurred': raw_data['firstTimestamp'],
                'last_occurred': raw_data['lastTimestamp'],
                'host_id': host_id,
            },
            'Severity': severity,
            'Message': raw_data['message'],
        }

        entities = [
            {
                'EntityID': raw_data['involvedObject']['name'],
                'EntityType': 'POD'
            },
            {
                'EntityID': raw_data['involvedObject']['namespace'],
                'EntityType': 'NAMESPACE'
            }
        ]

        if cluster_id:
            entities.append({
                'EntityID': cluster_id,
                'EntityType': 'CLUSTER'
            })

        if host_id:
            entities.append({
                'EntityID': host_id,
                'EntityType': 'HOST'
            })

        if host_ip:
            entities.append({
                'EntityID': host_ip,
                'EntityType': 'HOST'
            })

        links = [
            {
                'SourceEntityID': raw_data['involvedObject']['name'],
                'DestinationEntityID': raw_data['involvedObject']['namespace']
            }
        ]

        if host_id and cluster_id:
            links.append({
                'SourceEntityID': host_id,
                'DestinationEntityID': cluster_id
            })

        if cluster_id:
            links.append({
                'SourceEntityID': raw_data['involvedObject']['name'],
                'DestinationEntityID': cluster_id
            })

        if host_ip:
            links.append({
                'SourceEntityID': host_ip,
                'DestinationEntityID': raw_data['involvedObject']['name']
            })

        if host_ip and host_id:
            links.append({
                'SourceEntityID': host_ip,
                'DestinationEntityID': host_id
            })

        if host_id and raw_data['involvedObject']['name']:
            links.append({
                'SourceEntityID': raw_data['involvedObject']['name'],
                'DestinationEntityID': host_id
            })

        return TransformerOutput(
            processed_events=[processed_data],
            entities=entities,
            links=links
        )
