from typing import Optional

from ml_serve_model import Environment


class EnvironmentService:
    async def get_environment_by_name(self, name: str) -> Optional[Environment]:
        return await Environment.get_or_none(name=name)
