from typing import List
from pydantic import BaseModel, Field


class LabelValuesRequest(BaseModel):
    keys: List[str] = Field(..., description="List of label keys to get values for", min_items=1, max_items=20)
