from src.dto.schema.link import LinkCreate, LinkUpdate
from src.models.models import Link


class LinkService:

    def __init__(self):
        pass

    async def create_link(self, link: LinkCreate):
        link_obj = await Link.create(**link.dict())
        return link_obj

    async def get_link(self, link_id: int):
        return await Link.get(LinkID=link_id)

    async def update_link(self, link_id: int, link: LinkUpdate):
        link_obj = await Link.get(LinkID=link_id)
        await link_obj.update_from_dict(link.dict())
        await link_obj.save()
        return link_obj

    async def delete_link(self, link_id: int):
        link_obj = await Link.get(LinkID=link_id)
        await link_obj.delete()
        return link_obj

    async def get_all_links(self):
        return await Link.all()

    async def find_links_by_source_entity(self, source_entity_id: str):
        links = await Link.filter(SourceEntityID=source_entity_id)
        return links

    async def find_latest_link_by_source_entity(self, source_entity_id: str):
        link = await Link.filter(SourceEntityID=source_entity_id).order_by('-CreatedAt').first()
        return link

