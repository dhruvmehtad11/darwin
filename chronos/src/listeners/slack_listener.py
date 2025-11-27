from typing import Dict, Any

import aiohttp

from src.listeners.base_listener import BaseListener


class SlackListener(BaseListener):
    async def apply(self, processed_event: Dict[str, Any]):
        webhook_url = self.listener_record.webhook_url
        async with aiohttp.ClientSession() as session:
            await session.post(webhook_url, json=processed_event)
