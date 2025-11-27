from src.dto.schema.entity import EntityCreate, EntityUpdate
from src.models.models import Entity


class EntityService:
    async def create_entity(self, entity: EntityCreate):
        entity_obj = await Entity.create(**entity.dict())
        return entity_obj

    async def get_entity(self, entity_id: str):
        return await Entity.get(EntityID=entity_id)

    async def update_entity(self, entity_id: str, entity: EntityUpdate):
        entity_obj = await Entity.get(EntityID=entity_id)
        await entity_obj.update_from_dict(entity.dict())
        await entity_obj.save()
        return entity_obj

    async def delete_entity(self, entity_id: str):
        entity_obj = await Entity.get(EntityID=entity_id)
        await entity_obj.delete()
        return entity_obj

    async def get_all_entities(self):
        return await Entity.all()
