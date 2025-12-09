from typeguard import typechecked

from commons.kafka.event_entity import Event
from commons.kafka.kafka_producer import KafkaWriter


@typechecked
class EventPublisher:
    def __init__(self, env: str):
        if env == 'uat':
            self.topic = 'mlp_monitoring_uat'
        else:
            self.topic = 'mlp_monitoring'
        self.producer = KafkaWriter(env, self.topic)

    def send(self, event: Event, key=None):
        """
        For Sending Darwin Monitoring Events to Kafka
        :param event: Event Object
        :param key: key to be attached with event object. Default: None
        :returns: None
        """
        try:
            event = event.to_json()
            self.producer.write(event, key)
        except Exception as err:
            pass
#             print("Kafka Error:", err)
