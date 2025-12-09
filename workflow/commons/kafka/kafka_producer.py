import json
import uuid

from kafka import KafkaProducer
from typeguard import typechecked

from commons.constant.config import Config
from commons.utils.serializer import get_serializer


@typechecked
class KafkaWriter:

    def __init__(self, env: str, topic_name: str):
        self.bootstrap_servers = Config(env).monitoring_kafka_bootstrap_server
        self.producer = self.__create_kafka_producer()
        self.topic = topic_name

    def write(self, event, key: str = None) -> None:
        """
        writes message of list of records to a kafka topic
        :param key: key for record
        :param event: message
        :return: None
        """
        future = self.producer.send(topic=self.topic, key=key, value=json.dumps(event))
        future.get(timeout=60)

    def close(self) -> None:
        """
        closes the writer kafka producer object
        :return: None
        """
        if self.producer:
            self.producer.close()

    def __create_kafka_producer(self) -> KafkaProducer:
        return KafkaProducer(bootstrap_servers=self.bootstrap_servers,
                             key_serializer=get_serializer('STRING_SER'),
                             value_serializer=get_serializer('STRING_SER'),
                             acks='all',
                             compression_type='gzip',
                             retries=1,
                             linger_ms=10,
                             client_id=str(uuid.uuid4()))
