import os

CONFIG_MAP = {
    "local": {
        "kafkaUrl": "darwin-kafka-0.darwin-kafka-headless.darwin.svc.cluster.local:9092",
        "raw_event_topic": "raw-events",
        "processed_event_topic": "processed-events",
        "kafka_dlq_topic": "dlq-events",
        "kafka_consumer_name": "consumer-chronos",
        "chronos_app_layer": "http://localhost/chronos",
        "sqs_region": "us-east-1",
        "sqs_endpoint_url": "http://darwin-localstack:4566",
        "raw_event_queue": "chronos-raw-events-queue",
        "processed_event_queue": "chronos-processed-events-queue",
        "dlq_queue": "chronos-dlq-queue",
    }
}

SQS_STRATEGY = 'SQS'
KAFKA_STRATEGY = 'KAFKA'
QUEUE_STRATEGY = 'QUEUE'