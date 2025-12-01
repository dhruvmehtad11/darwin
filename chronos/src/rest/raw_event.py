from loguru import logger
from datetime import datetime
from typing import Optional, List

from fastapi import APIRouter, HTTPException, Header, Request, BackgroundTasks
import traceback

from src.config.ApplicationConfig import ApplicationConfig
from src.consumers.configs.config import Config
from src.dao.stream_writer_factory import get_stream_writer
from src.dto.schema.raw_event import RawEvent, RawEventCreate, SuccessfulResponse, TestEventResponse
from src.service.raw_events import RawEventService


class RawEventRouter:
    def __init__(self, env):
        self.config = Config(env)
        self.router = APIRouter()
        self.raw_event_service = RawEventService(env)
        self.register_routes()
        self.producer = get_stream_writer(env)

    def register_routes(self):
        self.router.post("/raw_events/", response_model=RawEvent)(self.create_raw_event)
        self.router.delete("/raw_events/{raw_event_id}", response_model=RawEvent)(self.delete_raw_event)
        self.router.get("/raw_events/{raw_event_id}", response_model=RawEvent)(self.read_raw_event)
        self.router.get("/raw_events/", response_model=List[RawEvent])(self.read_raw_events)
        self.router.post("/event", response_model=SuccessfulResponse)(self.create_event)
        self.router.post("/event/test", response_model=List[TestEventResponse])(self.test_event)

    async def create_raw_event(
            self,
            request: Request,
            x_event_source: str = Header(...),
            x_event_timestamp: Optional[str] = Header(None),
            content_type: str = Header(...),
    ):
        event_data = await request.body()

        if content_type == "application/json":
            event_data = event_data.decode('utf-8')
        elif content_type == "text/plain":
            event_data = event_data.decode('utf-8')
        elif content_type == "application/octet-stream":
            event_data = event_data
        else:
            raise HTTPException(status_code=400, detail="Unsupported content type")

        raw_event_create = RawEventCreate(
            Source=x_event_source,
            EventData=event_data,
            EventTimestamp=x_event_timestamp,
            ContentType=content_type,
        )
        created_event = await self.raw_event_service.create_raw_event(raw_event_create)

        return RawEvent(
            RawEventID=created_event.RawEventID,
            Source=created_event.Source,
            EventData=created_event.EventData,
            EventTimestamp=created_event.EventTimestamp.strftime("%Y-%m-%d %H:%M:%S"),
            IngestionTimestamp=created_event.IngestionTimestamp.strftime("%Y-%m-%d %H:%M:%S"),
            ContentType=created_event.ContentType
        )

    async def delete_raw_event(self, raw_event_id: int):
        return await self.raw_event_service.delete_raw_event(raw_event_id)

    async def read_raw_event(self, raw_event_id: int):
        raw_event = await self.raw_event_service.get_raw_event(raw_event_id)
        if not raw_event:
            raise HTTPException(status_code=404, detail="Raw Event not found")
        return RawEvent(
            RawEventID=raw_event.RawEventID,
            Source=raw_event.Source,
            EventData=raw_event.EventData,
            EventTimestamp=raw_event.EventTimestamp.strftime("%Y-%m-%d %H:%M:%S"),
            IngestionTimestamp=raw_event.IngestionTimestamp.strftime("%Y-%m-%d %H:%M:%S"),
            ContentType=raw_event.ContentType
        )

    async def read_raw_events(self):
        raw_events = await self.raw_event_service.get_all_raw_events()
        return [RawEvent(
            RawEventID=event.RawEventID,
            Source=event.Source,
            EventData=event.EventData,
            EventTimestamp=event.EventTimestamp.strftime("%Y-%m-%d %H:%M:%S"),
            IngestionTimestamp=event.IngestionTimestamp.strftime("%Y-%m-%d %H:%M:%S"),
            ContentType=event.ContentType
        ) for event in raw_events]

    async def create_event(
            self,
            request: Request,
            x_event_source: str = Header(...),
            x_event_timestamp: Optional[str] = Header(None),
            content_type: str = Header(...),
    ):
        try:
            event_data = await request.body()
            if x_event_timestamp is None:
                x_event_timestamp = str(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
            raw_event_create = RawEventCreate(
                Source=x_event_source,
                EventData=event_data,
                EventTimestamp=x_event_timestamp,
                ContentType=content_type,
            )

            try:
                event_obj = await self.raw_event_service.create_raw_event(raw_event_create)
                self.producer.write_record(
                    self.config.get_raw_event_destination,
                    {
                        "rawEventID": event_obj.RawEventID,
                    }
                )
                logger.info(f"Event created successfully with raw_event_id {event_obj.RawEventID} and sent to queue for processing via transformers")
            except Exception as e:
                logger.exception(f"Error writing to queue: {e}" )
                raise e
            return SuccessfulResponse(message="Event created successfully and sent for processing via transformers")
        except Exception as e:
            logger.exception(f"Error in creating event: {e}")
            raise HTTPException(status_code=500, detail=str(e))

    async def test_event(
            self,
            request: Request,
            x_event_source: str = Header(...),
            x_event_timestamp: Optional[str] = Header(None),
            content_type: str = Header(...),
    ):
        try:
            event_data = await request.body()
            if x_event_timestamp is None:
                x_event_timestamp = str(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
            import src.models.models
            event_obj = src.models.models.RawEvent(
                RawEventID=-1,
                Source=x_event_source,
                EventData=event_data,
                EventTimestamp=x_event_timestamp,
                IngestionTimestamp=datetime.now(),
                ContentType=content_type

            )
            return await self.raw_event_service.test_event(event_obj)
        except Exception as e:
            logger.exception(f"Error in testing event: {e}")
            raise HTTPException(status_code=500, detail=str(e))


raw_event_router = RawEventRouter(ApplicationConfig.get_config().env).router
