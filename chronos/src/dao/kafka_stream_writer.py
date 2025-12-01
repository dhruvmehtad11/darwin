import json

from kafka import KafkaProducer

from src.util.client_id_util import get_client_id
from src.util.ser_des_util import get_ser_des


class KafkaStreamWriter:
    def __init__(self, configs: dict):
        self._configs = configs
        self._kafka_producer = self.__create_kafka_producer()

    def write_batch(self, topic: str, events: list[dict], key: str = None) -> None:
        """
        writes message of list of dicts to a kafka topic
        :param topic: kafka topic
        :param key: key for record
        :param events: list of messages
        :return: None
        """
        for event in events:
            future = self._kafka_producer.send(topic=topic, key=key, value=json.dumps(event))
            future.get(timeout=60)
        self._kafka_producer.flush()

    def write_record(self, topic: str, record: dict, key: str = None) -> None:
        """
        writes message of list of records to a kafka topic
        :param topic: kafka topic
        :param key: key for record
        :param record: dict of messages
        :return: None
        """
        self._kafka_producer.send(topic=topic, key=key, value=json.dumps(record))

    def close(self) -> None:
        """
        closes the writer kafka producer object
        :return: None
        """
        if self._kafka_producer:
            self._kafka_producer.close()

    def __create_kafka_producer(self) -> KafkaProducer:
        return KafkaProducer(
            bootstrap_servers=self._configs.get("bootstrap_servers"),
            key_serializer=get_ser_des(self._configs.get("key_serializer", "STRING_SER")),
            value_serializer=get_ser_des(self._configs.get("value_serializer", "STRING_SER")),
            acks=self._configs.get("acks", "all"),
            compression_type=self._configs.get("compression_type", "gzip"),
            retries=self._configs.get("retries", 1),
            linger_ms=self._configs.get("linger_ms", 10),
            client_id=get_client_id(),
            reconnect_backoff_max_ms=self._configs.get("reconnect_backoff_max_ms", 1000),
            request_timeout_ms=self._configs.get("request_timeout_ms", 30000),
            api_version_auto_timeout_ms=5000
        )
