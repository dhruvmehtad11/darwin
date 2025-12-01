from loguru import logger

from src.consumers.configs.config import Config
from src.dao.stream_writer_factory import get_stream_writer
from src.service.event_processor_service import EventProcessor
from src.service.raw_events import RawEventService


class QueueEventProcessor:
    def __init__(self, env):
        self.config = Config(env)
        self.writer = get_stream_writer(env)
        self.event_processor = EventProcessor(env)
        self.raw_event_service = RawEventService(env)

    async def process(self, raw_event_id):
        try:
            raw_event = await self.raw_event_service.get_raw_event(raw_event_id)
            logger.info(f"Processing event {raw_event_id}")
            await self.event_processor.process_event(raw_event)
            return raw_event_id
        except Exception as e:
            logger.exception(f"Error processing raw event with ID - {raw_event_id} : {e}")
            self.writer.write_record(
                self.config.get_dlq_destination,
                {
                    "data": {
                        "RawEventID": raw_event_id,
                        "Error": str(e)
                    }
                }
            )
            return None
