from abc import ABC, abstractmethod
from typing import Union

from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput


class BasePythonTransformer(ABC):

    @abstractmethod
    async def is_transformer_applicable(self, event_data: RawEvent) -> bool:
        """
        Check if the transformer is applicable for the given event data.

        :param event_data: The raw event data as a string.
        :return: True if the transformer is applicable, False otherwise.
        """
        pass

    @abstractmethod
    async def transform(self, event_data: RawEvent) -> Union[TransformerOutput, None]:
        """
        Transform the raw event data into processed events, links, and entities.

        :param event_data: The raw event data as a string.
        :return: A dictionary containing processed events, links, and entities.
        """
        pass
