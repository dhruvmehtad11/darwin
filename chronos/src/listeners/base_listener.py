from abc import ABC, abstractmethod
from typing import Dict, Any


class BaseListener(ABC):
    def __init__(self, listener_record):
        self.listener_record = listener_record

    @abstractmethod
    async def apply(self, processed_event: Dict[str, Any]):
        pass
