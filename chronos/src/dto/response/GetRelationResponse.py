from pydantic import BaseModel, Field


class GetRelationResponse(BaseModel):
    relationId: int = Field(alias="relation_id")
    fromEntityId: int = Field(alias="from_entity_id")
    toEntityId: int = Field(alias="to_entity_id")
