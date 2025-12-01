import aiohttp
from aiohttp import ClientSession
from typing import Optional, Dict
from asyncio import Lock
from src.config.config import Config


class APIClient:
    def __init__(self, config: Config):
        self.config = config
        self._sessions: Dict[str, ClientSession] = {}
        self._retry_attempts = 3  # Retry logic for transient errors

    async def _get_session(self, base_url: str) -> ClientSession:
        """Retrieve or create a session for the given base URL."""
        if base_url not in self._sessions:
            self._sessions[base_url] = aiohttp.ClientSession(
                base_url=base_url, connector=aiohttp.TCPConnector(limit_per_host=10)
            )
        return self._sessions[base_url]

    async def _make_request(
        self, method: str, url: str, base_url: str, **kwargs
    ) -> dict:
        """Make a non-blocking HTTP request."""
        try:
            session = await self._get_session(base_url)
            async with session.request(method, url, **kwargs) as response:
                response.raise_for_status()
                return await response.json()

        except Exception as e:
            raise e

    async def get(
        self,
        url: str,
        base_url: str,
        body: Optional[Dict] = None,  # Added body parameter
        query_params: Optional[Dict] = None,
        headers: Optional[Dict] = None,
    ) -> dict:
        kwargs = {}
        if body:
            kwargs["json"] = body  # Include body in the request
        if query_params:
            kwargs["params"] = query_params
        if headers:
            kwargs["headers"] = headers
        return await self._make_request("GET", url, base_url, **kwargs)

    async def post(
        self,
        url: str,
        base_url: str,
        body: Optional[Dict] = None,
        query_params: Optional[Dict] = None,
        headers: Optional[Dict] = None,
    ) -> dict:
        kwargs = {}
        if body:
            kwargs["json"] = body
        if query_params:
            kwargs["params"] = query_params
        if headers:
            kwargs["headers"] = headers
        return await self._make_request("POST", url, base_url, **kwargs)

    async def put(
        self,
        url: str,
        base_url: str,
        body: Optional[Dict] = None,
        query_params: Optional[Dict] = None,
        headers: Optional[Dict] = None,
    ) -> dict:
        kwargs = {}
        if body:
            kwargs["json"] = body
        if query_params:
            kwargs["params"] = query_params
        if headers:
            kwargs["headers"] = headers
        return await self._make_request("PUT", url, base_url, **kwargs)

    async def delete(
        self,
        url: str,
        base_url: str,
        query_params: Optional[Dict] = None,
        headers: Optional[Dict] = None,
    ) -> dict:
        kwargs = {}
        if query_params:
            kwargs["params"] = query_params
        if headers:
            kwargs["headers"] = headers
        return await self._make_request("DELETE", url, base_url, **kwargs)

    async def close(self):
        """Clean up and close all open sessions."""
        for session in self._sessions.values():
            await session.close()
