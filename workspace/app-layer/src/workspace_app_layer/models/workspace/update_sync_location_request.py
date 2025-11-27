from pydantic import BaseModel


class UpdateSyncLocationRequest(BaseModel):
    codespace_id: int
    sync_location: str
