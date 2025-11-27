from src.dto.schema.source import SourceCreate, SourceUpdate
from src.models.models import Source


class SourceService:
    async def create_source(self, source: SourceCreate):
        source_obj = await Source.create(**source.dict())
        return source_obj

    async def get_source(self, source_id: int):
        return await Source.get(SourceID=source_id)

    async def update_source(self, source_id: int, source: SourceUpdate):
        source_obj = await Source.get(SourceID=source_id)
        await source_obj.update_from_dict(source.dict())
        await source_obj.save()
        return source_obj

    async def delete_source(self, source_id: int):
        source_obj = await Source.get(SourceID=source_id)
        await source_obj.delete()
        return source_obj

    async def get_all_sources(self):
        return await Source.all()
