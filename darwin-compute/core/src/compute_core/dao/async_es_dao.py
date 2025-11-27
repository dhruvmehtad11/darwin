from typing import Any, Dict, List
import logging

from elasticsearch import AsyncElasticsearch, ElasticsearchException

from compute_core.constant.config import Config

logger = logging.getLogger(__name__)


class AsyncEsDao:
    """
    Asynchronous Elasticsearch Data Access Object for managing document operations.

    Provides async methods for CRUD operations and search functionality
    against an Elasticsearch cluster.
    """

    def __init__(self, env: str = None):
        config = Config(env).es_config()
        self.es_client = AsyncElasticsearch(
            config.get("host"), http_auth=(config.get("username", ""), config.get("password", ""))
        )
        self.index = config.get("index")
        if not self.index:
            raise ValueError("Elasticsearch index configuration is required")

    async def health(self) -> bool:
        """
        Perform a simple health check against the ES cluster.

        :return: True if the cluster is reachable (ping succeeds), False otherwise.
        """
        try:
            return await self.es_client.ping()
        except ElasticsearchException as e:
            logger.warning(f"Elasticsearch health check failed: {e}")
            return False

    async def create(self, id: str, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Create or replace a document in the index.

        :param id: Document ID.
        :param data: Document body as a dict.
        :return: Elasticsearch response dict.
        :raises: ElasticsearchException if operation fails.
        """
        try:
            return await self.es_client.index(index=self.index, id=id, body=data)
        except ElasticsearchException as e:
            logger.error(f"Failed to create document with ID {id}: {e}")
            raise

    async def get(self, id: str) -> Dict[str, Any]:
        """
        Retrieve a document by ID.

        :param id: Document ID.
        :return: The document source.
        :raises: ElasticsearchException if operation fails.
        """
        if not id:
            raise ValueError("Document ID is required")

        try:
            resp = await self.es_client.get(index=self.index, id=id)
            return resp.get("_source", {})
        except ElasticsearchException as e:
            logger.error(f"Failed to get document with ID {id}: {e}")
            raise

    async def update(self, id: str, doc: Dict[str, Any]) -> Dict[str, Any]:
        """
        Partially update a document.

        :param id: Document ID.
        :param doc: Partial document to merge.
        :return: Elasticsearch response dict.
        :raises: ElasticsearchException if operation fails.
        """
        if not id:
            raise ValueError("Document ID is required")
        if not doc:
            raise ValueError("Update document body is required")

        try:
            return await self.es_client.update(index=self.index, id=id, body={"doc": doc})
        except ElasticsearchException as e:
            logger.error(f"Failed to update document with ID {id}: {e}")
            raise

    async def delete(self, id: str) -> Dict[str, Any]:
        """
        Delete a document by ID.

        :param id: Document ID.
        :return: Elasticsearch response dict.
        :raises: ElasticsearchException if operation fails.
        """
        if not id:
            raise ValueError("Document ID is required")

        try:
            return await self.es_client.delete(index=self.index, id=id)
        except ElasticsearchException as e:
            logger.error(f"Failed to delete document with ID {id}: {e}")
            raise

    async def search(self, body: dict) -> List[Dict[str, Any]]:
        """
        Search for documents using the Elasticsearch query DSL.
        :param body: Document body.
        :return: List of hit `_source` dicts.
        :raises: ElasticsearchException if operation fails.
        """
        try:
            resp = await self.es_client.search(index=self.index, body=body)
            hits = resp.get("hits", {}).get("hits", [])
            return [hit.get("_source", {}) for hit in hits]
        except ElasticsearchException as e:
            logger.error(f"Failed to execute search query: {e}")
            raise

    async def search_raw(self, body: dict) -> Dict[str, Any]:
        """
        Search for documents using the Elasticsearch query DSL.
        Returns the full Elasticsearch response including aggregations.

        :param body: Document body.
        :return: Full Elasticsearch response dict.
        :raises: ElasticsearchException if operation fails.
        """
        try:
            return await self.es_client.search(index=self.index, body=body)
        except ElasticsearchException as e:
            logger.error(f"Failed to execute search query: {e}")
            raise

    async def close(self):
        """
        Close the Elasticsearch client connection.
        """
        try:
            await self.es_client.close()
        except Exception as e:
            logger.warning(f"Error closing Elasticsearch client: {e}")

    async def __aenter__(self):
        """Support for async context manager."""
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        """Support for async context manager."""
        await self.close()
