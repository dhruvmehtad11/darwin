from typing import Dict

from aiokafka import AIOKafkaProducer

from src.listeners.base_listener import BaseListener


class KafkaListener(BaseListener):
    def __init__(self, listener_record):
        super().__init__(listener_record)
        self.producer = AIOKafkaProducer(
            bootstrap_servers=self.listener_record.bootstrap_servers
        )

    async def apply(self, processed_event: Dict[str, Any]):
        await self.producer.start()
        try:
            await self.producer.send_and_wait(
                self.listener_record.topic,
                processed_event.encode('utf-8')
            )
        finally:
            await self.producer.stop()
