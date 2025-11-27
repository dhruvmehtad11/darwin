from typing import List

from fastapi import APIRouter, HTTPException

from src.dto.schema.link import Link, LinkCreate, LinkUpdate
from src.service.links import LinkService


class LinkRouter:
    def __init__(self):
        self.router = APIRouter()
        self.link_service = LinkService()
        self.register_routes()

    def register_routes(self):
        self.router.post("/links/", response_model=Link)(self.create_link)
        self.router.get("/links/{link_id}", response_model=Link)(self.read_link)
        self.router.put("/links/{link_id}", response_model=Link)(self.update_link)
        self.router.delete("/links/{link_id}", response_model=Link)(self.delete_link)
        self.router.get("/links/", response_model=List[Link])(self.read_links)

    async def create_link(self, link: LinkCreate):
        return await self.link_service.create_link(link)

    async def read_link(self, link_id: int):
        link = await self.link_service.get_link(link_id)
        if not link:
            raise HTTPException(status_code=404, detail="Link not found")
        return link

    async def update_link(self, link_id: int, link: LinkUpdate):
        return await self.link_service.update_link(link_id, link)

    async def delete_link(self, link_id: int):
        return await self.link_service.delete_link(link_id)

    async def read_links(self):
        return await self.link_service.get_all_links()


link_router = LinkRouter().router
