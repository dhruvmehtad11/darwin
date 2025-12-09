from abc import abstractmethod


class Identifier:
    """Base class for the Identifier"""

    @abstractmethod
    def get_unique_id(self) -> str:
        """Returns the unique identifier for an entity"""
        pass
