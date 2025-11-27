import importlib
import json
from abc import ABC, abstractmethod
from typing import Dict, Any, List

from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput, ProcessedEventData
from src.transformers.registry import register_transformer


class TransformerStrategy(ABC):
    def __init__(self, transformer_record):
        self.transformer_record = transformer_record

    @abstractmethod
    async def apply(self, raw_event: RawEvent) -> TransformerOutput:
        pass

