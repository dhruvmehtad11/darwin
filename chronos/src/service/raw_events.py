from src.dto.schema.raw_event import RawEventCreate
from src.models.models import RawEvent, RawEventV2
from src.service.event_processor_service import EventProcessor


class RawEventService:
    def __init__(self, env):
        self.event_processor = EventProcessor(env)

    async def create_raw_event(self, raw_event: RawEventCreate):
        await  RawEventV2.create(**raw_event.dict())
        raw_event_obj = await RawEvent.create(**raw_event.dict())
        return raw_event_obj

    async def get_raw_event(self, raw_event_id: int):
        raw_event = await RawEvent.get(RawEventID=raw_event_id)
        return raw_event

    async def delete_raw_event(self, raw_event_id: int):
        raw_event_obj = await RawEvent.get(RawEventID=raw_event_id)
        await raw_event_obj.delete()
        return raw_event_obj

    async def get_all_raw_events(self, limit: int = 100):
        return await RawEvent.all().limit(limit)

    async def test_event(self, event: RawEvent):
        return await self.event_processor.test_event(event)

    async def create_event(self, event: RawEvent):
        await self.event_processor.process_event(event)
        return event
