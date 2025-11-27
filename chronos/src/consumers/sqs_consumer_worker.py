from loguru import logger
import json
import boto3
from src.service.queue_event_processor import QueueEventProcessor


class SQSConsumerWorker:
    def __init__(self, config: dict, env: str):
        # creating a separate logger for individual worker. As they only need to print in stdout or stderr
        self.config = config

        self.sqs_consumer = None
        self.create_sqs_consumer()

        self.stop_worker = False
        self.poll_timeout_ms = 3000
        self.is_closed = False
        self.event_processor = QueueEventProcessor(env)

    def create_sqs_consumer(self):
        try:
            client_config = {
                'service_name': 'sqs',
                'region_name': self.config.get("region", "us-east-1")
            }

            if self.config.get("endpoint_url"):
                client_config['endpoint_url'] = self.config.get("endpoint_url")

            self.sqs_consumer = boto3.client(**client_config)
            
            # Get queue URL
            queue_name = self.config.get("queue_name")
            response = self.sqs_consumer.get_queue_url(QueueName=queue_name)
            self.queue_url = response['QueueUrl']
            
            logger.info(f"SQS consumer subscribed to queue: {queue_name}")
            
        except Exception as e:
            logger.exception(f"Error while creating SQS consumer : {e}")
            raise e

    async def async_generator(self):
        # Define the items to iterate over
        my_list = [1, 2, 3, 4, 5]
        # Create an infinite loop using an asynchronous generator
        while True:
            # Yield each item from the list
            for item in my_list:
                yield item

    async def run(self) -> None:
        logger.info(f"run: {self.config.get('queue_name')}")
        try:
            async for _it in self.async_generator():
                # Poll SQS queue for messages
                response = self.sqs_consumer.receive_message(
                    QueueUrl=self.queue_url,
                    MaxNumberOfMessages=self.config.get("max_messages", 1000),
                    WaitTimeSeconds=int(self.poll_timeout_ms / 1000),  # Convert ms to seconds
                    VisibilityTimeout=self.config.get("visibility_timeout", 30)
                )
                
                messages = response.get('Messages', [])
                
                # Check if any messages were received
                if messages is None or len(messages) == 0:
                    continue
                    
                # Process each message
                for message in messages:
                    try:
                        # Extract raw event ID from message body
                        body = json.loads(message['Body'])
                        raw_event_id = body['rawEventID']
                        await self.event_processor.process(raw_event_id)
                        
                        # Delete message after successful processing
                        self.sqs_consumer.delete_message(
                            QueueUrl=self.queue_url,
                            ReceiptHandle=message['ReceiptHandle']
                        )
                        
                    except Exception as e:
                        logger.exception(f"Error processing SQS message: {e}")
                        # Message will become visible again after visibility timeout

                if self.stop_worker:
                    # Mark worker as closed
                    self.is_closed = True
                    break
                    
        except Exception as e:
            logger.exception(f"Error while running SQS consumer worker! with error : {e}")
            raise e

    def is_worker_closed(self) -> bool:
        """Check if the SQS consumer worker is closed"""
        return self.is_closed