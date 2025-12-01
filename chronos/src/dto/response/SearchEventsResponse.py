from src.dto.response.GetEventResponse import GetEventResponse


class SearchEventsResponse:
    hasNext: bool
    events: list[GetEventResponse]

    def __init__(self, events: list[GetEventResponse], hasNext: bool):
        self.events = events
        self.hasNext = hasNext

