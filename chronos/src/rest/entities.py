from typing import List

from fastapi import APIRouter, HTTPException

from src.dto.schema.entity import Entity, EntityCreate, EntityUpdate
from src.service.entities import EntityService


class EntityRouter:
    def __init__(self):
        self.router = APIRouter()
        self.entity_service = EntityService()
        self.register_routes()

    def register_routes(self):
        self.router.post("/entities/", response_model=Entity)(self.create_entity)
        self.router.get("/entities/{entity_id}", response_model=Entity)(self.read_entity)
        self.router.put("/entities/{entity_id}", response_model=Entity)(self.update_entity)
        self.router.delete("/entities/{entity_id}", response_model=Entity)(self.delete_entity)
        self.router.get("/entities/", response_model=List[Entity])(self.read_entities)

    async def create_entity(self, entity: EntityCreate):
        return await self.entity_service.create_entity(entity)

    async def read_entity(self, entity_id: str):
        entity_obj = await self.entity_service.get_entity(entity_id)
        if not entity_obj:
            raise HTTPException(status_code=404, detail="Entity not found")
        return entity_obj

    async def update_entity(self, entity_id: str, entity: EntityUpdate):
        return await self.entity_service.update_entity(entity_id, entity)

    async def delete_entity(self, entity_id: str):
        return await self.entity_service.delete_entity(entity_id)

    async def read_entities(self):
        return await self.entity_service.get_all_entities()


entity_router = EntityRouter().router
