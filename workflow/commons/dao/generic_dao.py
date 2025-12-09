from __future__ import annotations

from abc import ABC, abstractmethod
from typing import TypeVar, Generic, Optional, List

from commons.models.dao_response import DaoResponse
from commons.models.identifier import Identifier
from commons.models.meta_data import MetaData
from commons.models.search_query import SearchQuery

D = TypeVar('D', bound=MetaData)
Q = TypeVar('Q', bound=SearchQuery)
I = TypeVar('I', bound=Identifier)
Response = DaoResponse[D]
SearchResponse = DaoResponse[List[D]]


class Dao(ABC, Generic[D, I]):
    """Base class for Data Access Object."""

    @abstractmethod
    def create(self, data: D, identifier: Optional[I]) -> Response:
        pass

    @abstractmethod
    def read(self, identifier: I) -> Response:
        pass

    @abstractmethod
    def update(self, data: D, identifier: I) -> Response:
        pass

    @abstractmethod
    def delete(self, identifier: I) -> Response:
        pass

    @abstractmethod
    def search(self, query: Q) -> SearchResponse:
        pass

    @abstractmethod
    def aggregation_search(self, query: Q) -> SearchResponse:
        pass
