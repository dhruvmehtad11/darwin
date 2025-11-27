from abc import ABC, abstractmethod
from typing import Any


class Validator(ABC):
    @abstractmethod
    def validate(self, value: Any) -> bool:
        pass

    @abstractmethod
    def get_error_message(self) -> str:
        pass
