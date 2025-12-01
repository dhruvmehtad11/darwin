from pydantic import BaseModel


class FolderContentsRequest(BaseModel):
    codespace_id: int
    folder_path: str
