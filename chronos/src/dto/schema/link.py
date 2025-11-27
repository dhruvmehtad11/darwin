from datetime import datetime
from pydantic import BaseModel


class LinkBase(BaseModel):
    SourceEntityID: str
    DestinationEntityID: str


class LinkCreate(LinkBase):
    pass


class LinkUpdate(LinkBase):
    pass


class LinkInDB(LinkBase):
    LinkID: int
    CreatedAt: datetime

    class Config:
        json_encoders = {
            datetime: lambda v: v.strftime("%Y-%m-%d %H:%M:%S")
        }


class Link(LinkInDB):
    pass
