from src.consumers.configs.config_constants import CONFIG_MAP, SQS_STRATEGY, KAFKA_STRATEGY, QUEUE_STRATEGY
from os import environ


class Config:
    def __init__(self, env):
        self._config = CONFIG_MAP[env]
        self._queue_strategy = environ.get(QUEUE_STRATEGY, SQS_STRATEGY).upper()

    @property
    def get_kafka_url(self):
        return self._config.get("kafkaUrl")

    @property
    def get_raw_event_topic(self):
        return self._config.get("raw_event_topic")

    @property
    def get_processed_event_topic(self):
        return self._config.get("processed_event_topic")

    @property
    def get_kafka_dlq_topic(self):
        return self._config.get("kafka_dlq_topic")

    @property
    def get_kafka_consumer_name(self):
        return self._config.get("kafka_consumer_name")

    @property
    def get_chronos_app_layer_url(self):
        return self._config.get("chronos_app_layer")

    @property
    def get_raw_event_queue(self):
        return self._config.get("raw_event_queue")

    @property
    def get_processed_event_queue(self):
        return self._config.get("processed_event_queue")

    @property
    def get_sqs_dlq_queue(self):
        return self._config.get("dlq_queue")

    @property
    def get_raw_event_destination(self):
        if self._queue_strategy == SQS_STRATEGY:
            return self.get_raw_event_queue
        return self.get_raw_event_topic

    @property
    def get_processed_event_destination(self):
        if self._queue_strategy == SQS_STRATEGY:
            return self.get_processed_event_queue
        return self.get_processed_event_topic

    @property
    def get_dlq_destination(self):
        if self._queue_strategy == SQS_STRATEGY:
            return self.get_sqs_dlq_queue
        return self.get_kafka_dlq_topic
