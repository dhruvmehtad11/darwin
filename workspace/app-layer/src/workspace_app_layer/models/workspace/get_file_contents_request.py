from pydantic import BaseModel


class GetFileContents(BaseModel):
    source_path: str
