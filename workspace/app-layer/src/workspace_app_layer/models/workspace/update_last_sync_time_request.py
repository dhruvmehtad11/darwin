from pydantic import BaseModel


class UpdateLastSyncTimeRequest(BaseModel):
    codespace_id: int
