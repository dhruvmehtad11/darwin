import aioredis
from typing import Any, Optional, List, Dict


class RedisClient:
    def __init__(self, redis_url: str, max_connections: int = 10):
        self.redis_url = redis_url
        self.max_connections = max_connections
        self.pool = None

    async def initialize(self):
        """Initialize the Redis connection pool."""
        self.pool = await aioredis.from_url(self.redis_url, max_connections=self.max_connections, decode_responses=True)

    async def get(self, key: str) -> Optional[str]:
        """Get the value of a key."""
        async with self.pool.get() as conn:
            return await conn.get(key)

    async def set(self, key: str, value: Any, expire: Optional[int] = None) -> bool:
        """Set the value of a key with an optional expiration."""
        async with self.pool.get() as conn:
            return await conn.set(key, value, ex=expire)

    async def delete(self, key: str) -> int:
        """Delete a key."""
        async with self.pool.get() as conn:
            return await conn.delete(key)

    async def lpush(self, key: str, *values: Any) -> int:
        """Push values onto the head of a list."""
        async with self.pool.get() as conn:
            return await conn.lpush(key, *values)

    async def rpop(self, key: str) -> Optional[str]:
        """Remove and get the last element in a list."""
        async with self.pool.get() as conn:
            return await conn.rpop(key)

    async def hgetall(self, key: str) -> Dict[str, str]:
        """Get all fields and values in a hash."""
        async with self.pool.get() as conn:
            return await conn.hgetall(key)

    async def close(self):
        """Close the Redis connection pool."""
        if self.pool:
            await self.pool.close()
            await self.pool.wait_closed()

    async def hget(self, key: str, field: str) -> Optional[str]:
        """Get the value of a hash field."""
        async with self.pool.get() as conn:
            return await conn.hget(key, field)

    async def hset(self, key: str, field: str, value: Any) -> int:
        """Set the value of a hash field."""
        async with self.pool.get() as conn:
            return await conn.hset(key, field, value)

    async def ttl(self, key: str) -> int:
        """Get the time to live for a key."""
        async with self.pool.get() as conn:
            return await conn.ttl(key)

    async def expire(self, key: str, time: int) -> bool:
        """Set a key's time to live in seconds."""
        async with self.pool.get() as conn:
            return await conn.expire(key, time)

    async def expire_increase(self, key: str, additional_time: int) -> bool:
        """Increase a key's time to live by additional seconds."""
        async with self.pool.get() as conn:
            current_ttl = await conn.ttl(key)
            if current_ttl > 0:
                return await conn.expire(key, current_ttl + additional_time)
            return False
