from pydantic import BaseModel

from src.dto.request.AddEventRequest import AddEventRequest


class AddEventsRequest(BaseModel):
    events: list[AddEventRequest]
