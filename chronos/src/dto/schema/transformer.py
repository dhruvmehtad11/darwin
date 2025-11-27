from typing import Optional, Dict, List

from pydantic import BaseModel


class TransformerBase(BaseModel):
    TransformerName: str
    TransformerType: str
    Description: str
    SourceID: int
    ProcessType: str


class TransformerCreate(TransformerBase):
    pass


class TransformerUpdate(TransformerBase):
    pass


class TransformerInDB(TransformerBase):
    TransformerID: int


class Transformer(TransformerInDB):
    pass


class JSONTransformerBase(TransformerBase):
    MatchingEntity: str
    MatchingPath: str
    MatchingValue: Optional[str] = None
    EventMappings: List
    EntityPath: str
    EntityType: str
    EventType: str
    Relations: List
    Message: Optional[str] = None
    Severity: Optional[str] = None


class JSONTransformerCreate(JSONTransformerBase):
    pass


class JSONTransformerUpdate(JSONTransformerBase):
    pass


class JSONTransformerInDB(JSONTransformerBase):
    TransformerID: int


class JSONTransformerResponse(JSONTransformerInDB):
    pass


class PythonTransformerBase(TransformerBase):
    ScriptPath: str
    ClassName: str


class PythonTransformerCreate(PythonTransformerBase):
    pass


class PythonTransformerUpdate(PythonTransformerBase):
    pass


class PythonTransformerInDB(PythonTransformerBase):
    TransformerID: int


class PythonTransformerResponse(PythonTransformerInDB):
    pass
