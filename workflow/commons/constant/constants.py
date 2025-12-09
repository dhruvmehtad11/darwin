from enum import Enum


class Entity(str, Enum):
    """Entity enumeration for event tracking"""
    WORKFLOW = 'WORKFLOW'


class State(str, Enum):
    """Base State enumeration for event states"""
    pass


CONFIGS_MAP = {
    'local': {
        'elasticsearch.url': 'http://0.0.0.0:9200',
        'jfrog.url': 'http://0.0.0.0:9200'
    },
    'test': {
        'elasticsearch.url': 'localhost:9200',

        'jfrog.url': 'localhost:9200',
        'monitoring.kafka.bootstrap_server': '0.0.0.0:9092'
    },
    'prod': {
        'elasticsearch.url': 'http://0.0.0.0:9200',
        'jfrog.url': 'http://0.0.0.0:9200'
    },
    'stag': {
        'elasticsearch.url': 'http://mlp-es-dev-9051.dream11-stag.local:9200',
        'jfrog.url': 'http://0.0.0.0:9200'
    }
}
USAGE_METRIC_KAFKA_ENDPOINTS = {
    'stag': 'usage-metric-kafka-mlp-01.dream11-stag.local:9092',
    'prod': 'ml-platform-kafka.dream11.local:9092',
    'local': '0.0.0.0:9092'
}
USAGE_METRIC_KAFKA_TOPICS = {
    'stag': 'usage_logs_stag',
    'prod': 'usage_logs_prod',
    'local': 'usage_logs_local'
}
