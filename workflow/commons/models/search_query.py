from abc import abstractmethod


class SearchQuery:
    """Base class for the search query"""

    @abstractmethod
    def get_query(self):
        """Returns the search query object."""
        pass
