from typing import Dict, Any

import aiohttp

from src.listeners.base_listener import BaseListener


class APIListener(BaseListener):
    async def apply(self, processed_event: Dict[str, Any]):
        api_url = self.listener_record.api_url
        async with aiohttp.ClientSession() as session:
            await session.post(api_url, json=processed_event)
