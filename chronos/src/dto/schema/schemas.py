# from datetime import datetime
# from typing import Optional
#
# from pydantic import BaseModel
#
#
# class SourceBase(BaseModel):
#     SourceName: str
#     Description: str
#     EventFormatType: str
#
#
# class SourceCreate(SourceBase):
#     pass
#
#
# class SourceUpdate(SourceBase):
#     pass
#
#
# class SourceInDB(SourceBase):
#     SourceID: int
#
#
# class Source(SourceInDB):
#     pass
#
#
# class RawEventBase(BaseModel):
#     Source: str
#     EventData: bytes
#     EventTimestamp: Optional[str] = None
#     ContentType: str
#
#
# class RawEventCreate(RawEventBase):
#     pass
#
#
# class RawEventUpdate(RawEventBase):
#     pass
#
#
# class RawEventInDB(RawEventBase):
#     RawEventID: int
#     IngestionTimestamp: str
#
#     class Config:
#         json_encoders = {
#             datetime: lambda v: v.strftime("%Y-%m-%d %H:%M:%S")
#         }
#
#
# class RawEvent(RawEventInDB):
#     class Config:
#         json_encoders = {
#             datetime: lambda v: v.strftime("%Y-%m-%d %H:%M:%S")
#         }
#
#
# from pydantic import BaseModel
# from typing import Optional, Dict
#
#
# class TransformerBase(BaseModel):
#     TransformerName: str
#     TransformerType: str
#     Description: str
#     SourceID: int
#     ProcessType: str
#
#
# class TransformerCreate(TransformerBase):
#     pass
#
#
# class TransformerUpdate(TransformerBase):
#     pass
#
#
# class TransformerInDB(TransformerBase):
#     TransformerID: int
#
#
# class Transformer(TransformerInDB):
#     pass
#
#
# class JSONTransformerBase(TransformerBase):
#     MatchingEntity: str
#     MatchingPath: str
#     MatchingValue: Optional[str] = None
#     EventMappings: Dict
#     EntityPath: str
#     EntityType: str
#     EventType: str
#     Relations: Dict
#
#
# class JSONTransformerCreate(JSONTransformerBase):
#     pass
#
#
# class JSONTransformerUpdate(JSONTransformerBase):
#     pass
#
#
# class JSONTransformerInDB(JSONTransformerBase):
#     TransformerID: int
#
#
# class JSONTransformerResponse(JSONTransformerInDB):
#     pass
#
#
# class PythonTransformerBase(TransformerBase):
#     ScriptPath: str
#     FunctionName: str
#
#
# class PythonTransformerCreate(PythonTransformerBase):
#     pass
#
#
# class PythonTransformerUpdate(PythonTransformerBase):
#     pass
#
#
# class PythonTransformerInDB(PythonTransformerBase):
#     TransformerID: int
#
#
# class PythonTransformerResponse(PythonTransformerInDB):
#     pass
#
#
# class ProcessedEventBase(BaseModel):
#     RawEventID: int
#     EventType: str
#     EventData: Dict
#     EntityID: Optional[str]
#     SourceID: int
#     TransformerID: int
#
#
# class ProcessedEventCreate(ProcessedEventBase):
#     pass
#
#
# class ProcessedEventInDB(ProcessedEventBase):
#     ProcessedEventID: int
#     IngestionTime: datetime
#     ProcessedTime: datetime
#
#
# class ProcessedEventResponse(ProcessedEventInDB):
#     pass
#
#
# class LinkBase(BaseModel):
#     SourceEntityID: str
#     DestinationEntityID: str
#
#
# class LinkCreate(LinkBase):
#     pass
#
#
# class LinkUpdate(LinkBase):
#     pass
#
#
# class LinkInDB(LinkBase):
#     LinkID: int
#     CreatedAt: datetime
#
#     class Config:
#         json_encoders = {
#             datetime: lambda v: v.strftime("%Y-%m-%d %H:%M:%S")
#         }
#
#
# class Link(LinkInDB):
#     pass
#
#
# class EntityBase(BaseModel):
#     EntityID: str
#     EntityType: str
#
#
# class EntityCreate(EntityBase):
#     pass
#
#
# class EntityUpdate(EntityBase):
#     pass
#
#
# class EntityInDB(EntityBase):
#     pass
#
#
# class Entity(EntityInDB):
#     pass
