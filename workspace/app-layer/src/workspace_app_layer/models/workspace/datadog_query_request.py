from pydantic import BaseModel


class DatadogQueryRequest(BaseModel):
    unix_start_time: int
    unix_end_time: int
    datadog_query: str
