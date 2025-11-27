from pydantic import BaseModel


class SourceBase(BaseModel):
    SourceName: str
    Description: str
    EventFormatType: str


class SourceCreate(SourceBase):
    pass


class SourceUpdate(SourceBase):
    pass


class SourceInDB(SourceBase):
    SourceID: int


class Source(SourceInDB):
    pass
