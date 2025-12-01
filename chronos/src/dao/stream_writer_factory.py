from os import environ

from src.consumers.configs.kafka_config import get_kafka_config
from src.consumers.configs.sqs_config import get_sqs_config
from src.dao.kafka_stream_writer import KafkaStreamWriter
from src.dao.sqs_stream_writer import SQSStreamWriter
from src.consumers.configs.config_constants import SQS_STRATEGY, KAFKA_STRATEGY, QUEUE_STRATEGY


def get_stream_writer(env: str):
    """
    Factory function to get a stream writer instance based on the QUEUE environment variable.
    """
    queue_strategy = environ.get(QUEUE_STRATEGY, SQS_STRATEGY).upper()

    if queue_strategy == SQS_STRATEGY:
        return SQSStreamWriter(get_sqs_config(env)["writer_configs"])
    elif queue_strategy == KAFKA_STRATEGY:
        return KafkaStreamWriter(get_kafka_config(env))
    else:
        raise ValueError(f"Unknown QUEUE strategy: {queue_strategy}")
