from src.consumers.configs.config import Config


def get_kafka_config(env: str):
    config = Config(env)
    kafka_configs = {
        "consumer_name": config.get_kafka_consumer_name,
        "enable_auto_commit": False,
        "bootstrap_servers": config.get_kafka_url,
        "key_deserializer": "STRING_DES",
        "value_deserializer": "JSON_DES_SKIP_ERRORS",
        "header_deserializer": None,
        "auto_offset_reset": "latest",
        "max_poll_records": 1000,
        "max_poll_interval_ms": 60000,
        "is_remote_consumer": False,
        "session.timeout.ms": 100000,
        "max.partition.fetch.bytes": 10000000,
        "topic_name": config.get_raw_event_topic,
        "writer_configs": {
            "bootstrap_servers": config.get_kafka_url,
            "key_serializer": "STRING_SER",
            "value_serializer": "STRING_SER",
            "linger_ms": 10,
            "compression_type": "gzip",
            "retries": 3,
            "request_timeout_ms": 30000,
            "reconnect_backoff_max_ms": 1000,
        },
    }

    return kafka_configs
