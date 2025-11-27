from loguru import logger
import os
from kafka import KafkaConsumer

from src.service.queue_event_processor import QueueEventProcessor
from src.util.client_id_util import get_client_id
from src.util.ser_des_util import get_ser_des


MAX_RESTARTS_REMOTE_WORKER = 10
WORKER_NUM_CPUS = 1
CLIENT_ID = get_client_id()
USERNAME = os.environ.get("APP_USERNAME", "admin")
PASSWORD = os.environ.get("APP_PASSWORD", "admin")
SASL_USERNAME = os.environ.get("SASL_USERNAME", None)
SASL_PASSWORD = os.environ.get("SASL_PASSWORD", None)
SECURITY_PROTOCOL = os.environ.get("SECURITY_PROTOCOL", "PLAINTEXT")
SASL_MECHANISM = os.environ.get("SASL_MECHANISM")


class ConsumerWorker:
    def __init__(self, config: dict, env: str):
        # creating a separate logger for individual worker. As they only need to print in stdout or stderr
        self.config = config

        self.kafka_consumer = None
        self.create_kafka_consumer()

        self.stop_worker = False
        self.poll_timeout_ms = 3000
        self.is_closed = False
        self.event_processor = QueueEventProcessor(env)

    def create_kafka_consumer(self):
        try:
            self.kafka_consumer = KafkaConsumer(
                bootstrap_servers=self.config.get("bootstrap_servers"),
                client_id=CLIENT_ID,
                group_id=self.config.get("consumer_name"),
                key_deserializer=get_ser_des(self.config.get("key_deserializer", "STRING_DES")),
                value_deserializer=get_ser_des(self.config.get("value_deserializer", "JSON_DES")),
                auto_offset_reset=self.config.get("auto_offset_reset", "earliest"),
                enable_auto_commit=self.config.get("enable_auto_commit", False),
                max_poll_records=self.config.get("max_poll_records", 50),
                max_poll_interval_ms=self.config.get("max_poll_interval_ms", 600000),
                heartbeat_interval_ms=500,
                security_protocol=SECURITY_PROTOCOL,
                sasl_mechanism=SASL_MECHANISM,
                sasl_plain_username=SASL_USERNAME,
                sasl_plain_password=SASL_PASSWORD,
                max_partition_fetch_bytes=self.config.get("max.partition.fetch.bytes", 1000000),
                consumer_timeout_ms=self.config.get("session.timeout.ms", 150000),
            )
            self.kafka_consumer.subscribe([self.config.get("topic_name")])
        except Exception as e:
            logger.exception(f"Error while creating kafka consumer : {e}")
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
        logger.info(f"run: {self.config.get('topic_name')}")
        try:
            async for _it in self.async_generator():
                tp_records_dict = self.kafka_consumer.poll(timeout_ms=self.poll_timeout_ms)
                if tp_records_dict is None or len(tp_records_dict.items()) == 0:
                    continue
                for tp, records in tp_records_dict.items():
                    for record in records:
                        raw_event_id = record.value['rawEventID']
                        await self.event_processor.process(raw_event_id)

                self.kafka_consumer.commit()
                if self.stop_worker:
                    self.kafka_consumer.close()
                    self.is_closed = True
                    break
        except Exception as e:
            logger.exception(f"Error while running consumer worker! with error : {e}")
            raise e
