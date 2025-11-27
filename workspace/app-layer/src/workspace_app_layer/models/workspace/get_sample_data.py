from pydantic import BaseModel


class GetSampleData(BaseModel):
    env: str
    source: str
    dataSource: str
