import json
import boto3
from loguru import logger

from src.util.client_id_util import get_client_id


class SQSStreamWriter:
    def __init__(self, configs: dict):
        self._configs = configs
        self._sqs_client = self.__create_sqs_client()
        self._queue_urls = {}  # Cache queue URLs

    def write_record(self, queue_name: str, record: dict, key: str = None) -> None:
        """
        writes a single message to an SQS queue
        :param queue_name: SQS queue name
        :param key: message group id for FIFO queues (optional)
        :param record: dict message
        :return: None
        """
        queue_url = self._get_queue_url(queue_name)
        
        message_body = json.dumps(record)
        
        try:
            send_params = {
                'QueueUrl': queue_url,
                'MessageBody': message_body
            }
            
            # Add FIFO queue parameters if needed
            if key and queue_name.endswith('.fifo'):
                send_params['MessageGroupId'] = key
                send_params['MessageDeduplicationId'] = f"{get_client_id()}-{hash(message_body)}"
            
            self._sqs_client.send_message(**send_params)
            
        except Exception as e:
            logger.exception(f"Error sending message to SQS queue {queue_name}: {e}")
            raise e

    def _get_queue_url(self, queue_name: str) -> str:
        """Get queue URL, with caching"""
        if queue_name not in self._queue_urls:
            try:
                response = self._sqs_client.get_queue_url(QueueName=queue_name)
                self._queue_urls[queue_name] = response['QueueUrl']
            except self._sqs_client.exceptions.QueueDoesNotExist:
                logger.error(f"SQS queue {queue_name} does not exist")
                raise ValueError(f"SQS queue {queue_name} does not exist")
        
        return self._queue_urls[queue_name]

    def __create_sqs_client(self):
        """Create SQS client with configuration"""
        client_config = {
            'service_name': 'sqs',
            'region_name': self._configs.get("region", "us-east-1")
        }
        
        if self._configs.get("endpoint_url"):
            client_config['endpoint_url'] = self._configs.get("endpoint_url")

        return boto3.client(**client_config)
