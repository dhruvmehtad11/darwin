from datetime import datetime
from typing import Optional, List

from pydantic import BaseModel

from src.dto.schema.base_transformers import ProcessedEventData, TransformerOutput


class RawEventBase(BaseModel):
    Source: str
    EventData: bytes
    EventTimestamp: Optional[str] = None
    ContentType: str


class RawEventCreate(RawEventBase):
    pass


class RawEventUpdate(RawEventBase):
    pass


class RawEventInDB(RawEventBase):
    RawEventID: int
    IngestionTimestamp: str

    class Config:
        json_encoders = {
            datetime: lambda v: v.strftime("%Y-%m-%d %H:%M:%S")
        }


class RawEvent(RawEventInDB):
    class Config:
        json_encoders = {
            datetime: lambda v: v.strftime("%Y-%m-%d %H:%M:%S")
        }


class SuccessfulResponse(BaseModel):
    message: str
    status_code: int = 200


class TestEventResponse(BaseModel):
    source_id: int
    transformer_id: int
    processed_data: Optional[TransformerOutput] = None
    status: str
    message: str
