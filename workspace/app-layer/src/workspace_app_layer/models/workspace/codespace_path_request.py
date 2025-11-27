from pydantic import BaseModel


class CodespacePathRequest(BaseModel):
    codespace_path: str
