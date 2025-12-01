from typing import List

from pydantic import BaseModel


class UpdateTagsEntity(BaseModel):
    tags: List[str]
