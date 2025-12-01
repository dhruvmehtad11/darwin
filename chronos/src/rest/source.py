from typing import List

from fastapi import APIRouter, HTTPException

from src.dto.schema.source import Source, SourceCreate, SourceUpdate
from src.service.sources import SourceService


class SourceRouter:
    def __init__(self):
        self.router = APIRouter()
        self.source_service = SourceService()
        self.register_routes()

    def register_routes(self):
        self.router.post("/sources/", response_model=Source)(self.create_source)
        self.router.get("/sources/{source_id}", response_model=Source)(self.read_source)
        self.router.put("/sources/{source_id}", response_model=Source)(self.update_source)
        self.router.delete("/sources/{source_id}", response_model=Source)(self.delete_source)
        self.router.get("/sources/", response_model=List[Source])(self.read_sources)

    async def create_source(self, source: SourceCreate):
        return await self.source_service.create_source(source)

    async def read_source(self, source_id: int):
        source_obj = await self.source_service.get_source(source_id)
        if not source_obj:
            raise HTTPException(status_code=404, detail="Source not found")
        return source_obj

    async def update_source(self, source_id: int, source: SourceUpdate):
        return await self.source_service.update_source(source_id, source)

    async def delete_source(self, source_id: int):
        return await self.source_service.delete_source(source_id)

    async def read_sources(self):
        return await self.source_service.get_all_sources()


source_router = SourceRouter().router
