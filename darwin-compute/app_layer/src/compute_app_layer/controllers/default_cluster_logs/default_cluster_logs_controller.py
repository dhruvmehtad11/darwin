from fastapi.responses import JSONResponse

from compute_core.service.event_service import EventService


async def get_default_events() -> JSONResponse:
    event_service = EventService()
    return await event_service.get_default_events()
