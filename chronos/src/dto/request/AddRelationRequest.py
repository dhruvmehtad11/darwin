from typing import Optional

from pydantic import BaseModel, Field

from datetime import datetime


class AddRelationRequest(BaseModel):
    from_entity_id: str = Field(alias="fromEntityId")
    to_entity_id: str = Field(alias="toEntityId")
