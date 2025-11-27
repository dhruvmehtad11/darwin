from pydantic import BaseModel


class CustomRuntimeRequest(BaseModel):
    runtime: str
    image: str
    namespace: str
    created_by: str
    type: str
