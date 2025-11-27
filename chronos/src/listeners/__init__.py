from src.listeners.registry import register_listener
from src.listeners.slack_listener import SlackListener
from src.listeners.kafka_listener import KafkaListener
from src.listeners.api_listener import APIListener

@register_listener('SlackListener')
class SlackListener(SlackListener):
    pass

@register_listener('KafkaListener')
class KafkaListener(KafkaListener):
    pass

@register_listener('APIListener')
class APIListener(APIListener):
    pass