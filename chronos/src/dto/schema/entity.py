from pydantic import BaseModel


class EntityBase(BaseModel):
    EntityID: str
    EntityType: str


class EntityCreate(EntityBase):
    pass


class EntityUpdate(EntityBase):
    pass


class EntityInDB(EntityBase):
    pass


class Entity(EntityInDB):
    pass
