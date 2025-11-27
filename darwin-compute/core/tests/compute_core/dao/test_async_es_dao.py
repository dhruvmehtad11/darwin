import unittest
from unittest.mock import patch, AsyncMock

import pytest
from elasticsearch import ElasticsearchException

from compute_core.constant.config import Config
from compute_core.dao.async_es_dao import AsyncEsDao


class TestAsyncEsDao(unittest.TestCase):
    @patch("compute_core.dao.async_es_dao.Config")
    @patch("compute_core.dao.async_es_dao.AsyncElasticsearch")
    def setUp(self, mock_async_es, mock_config):
        # Mock config
        self.mock_config = mock_config.return_value
        self.mock_config.es_config.return_value = Config("local").es_config()

        # Mock Elasticsearch client
        self.mock_es_client = AsyncMock()
        mock_async_es.return_value = self.mock_es_client

        # Create the DAO instance
        self.async_es_dao = AsyncEsDao(env="test")

    @pytest.mark.asyncio
    async def test_health_success(self):
        """Test health check when ES is reachable"""
        self.mock_es_client.ping.return_value = True

        result = await self.async_es_dao.health()

        self.assertTrue(result)
        self.mock_es_client.ping.assert_called_once()

    @pytest.mark.asyncio
    async def test_health_failure(self):
        """Test health check when ES is unreachable"""
        self.mock_es_client.ping.side_effect = ElasticsearchException("Connection failed")

        result = await self.async_es_dao.health()

        self.assertFalse(result)
        self.mock_es_client.ping.assert_called_once()

    @pytest.mark.asyncio
    async def test_create_success(self):
        """Test successful document creation"""
        test_id = "test_doc_id"
        test_data = {"field1": "value1", "field2": "value2"}
        expected_response = {"_id": test_id, "_index": "test_index", "result": "created"}

        self.mock_es_client.index.return_value = expected_response

        result = await self.async_es_dao.create(test_id, test_data)

        self.assertEqual(result, expected_response)
        self.mock_es_client.index.assert_called_once_with(index="test_index", id=test_id, body=test_data)

    @pytest.mark.asyncio
    async def test_create_failure(self):
        """Test document creation failure"""
        test_id = "test_doc_id"
        test_data = {"field1": "value1"}

        self.mock_es_client.index.side_effect = ElasticsearchException("Index error")

        with self.assertRaises(ElasticsearchException):
            await self.async_es_dao.create(test_id, test_data)

    @pytest.mark.asyncio
    async def test_get_success(self):
        """Test successful document retrieval"""
        test_id = "test_doc_id"
        test_source = {"field1": "value1", "field2": "value2"}
        mock_response = {"_source": test_source, "_id": test_id}

        self.mock_es_client.get.return_value = mock_response

        result = await self.async_es_dao.get(test_id)

        self.assertEqual(result, test_source)
        self.mock_es_client.get.assert_called_once_with(index="test_index", id=test_id)

    @pytest.mark.asyncio
    async def test_get_missing_source(self):
        """Test document retrieval when _source is missing"""
        test_id = "test_doc_id"
        mock_response = {"_id": test_id}  # No _source field

        self.mock_es_client.get.return_value = mock_response

        result = await self.async_es_dao.get(test_id)

        self.assertEqual(result, {})

    @pytest.mark.asyncio
    async def test_get_failure(self):
        """Test document retrieval failure"""
        test_id = "test_doc_id"

        self.mock_es_client.get.side_effect = ElasticsearchException("Document not found")

        with self.assertRaises(ElasticsearchException):
            await self.async_es_dao.get(test_id)

    @pytest.mark.asyncio
    async def test_update_success(self):
        """Test successful document update"""
        test_id = "test_doc_id"
        test_doc = {"field1": "updated_value"}
        expected_response = {"_id": test_id, "result": "updated"}

        self.mock_es_client.update.return_value = expected_response

        result = await self.async_es_dao.update(test_id, test_doc)

        self.assertEqual(result, expected_response)
        self.mock_es_client.update.assert_called_once_with(index="test_index", id=test_id, body={"doc": test_doc})

    @pytest.mark.asyncio
    async def test_update_failure(self):
        """Test document update failure"""
        test_id = "test_doc_id"
        test_doc = {"field1": "updated_value"}

        self.mock_es_client.update.side_effect = ElasticsearchException("Update failed")

        with self.assertRaises(ElasticsearchException):
            await self.async_es_dao.update(test_id, test_doc)

    @pytest.mark.asyncio
    async def test_delete_success(self):
        """Test successful document deletion"""
        test_id = "test_doc_id"
        expected_response = {"_id": test_id, "result": "deleted"}

        self.mock_es_client.delete.return_value = expected_response

        result = await self.async_es_dao.delete(test_id)

        self.assertEqual(result, expected_response)
        self.mock_es_client.delete.assert_called_once_with(index="test_index", id=test_id)

    @pytest.mark.asyncio
    async def test_delete_failure(self):
        """Test document deletion failure"""
        test_id = "test_doc_id"

        self.mock_es_client.delete.side_effect = ElasticsearchException("Delete failed")

        with self.assertRaises(ElasticsearchException):
            await self.async_es_dao.delete(test_id)

    @pytest.mark.asyncio
    async def test_search_success(self):
        """Test successful search operation"""
        test_query = {"match": {"field1": "value1"}}
        test_size = 20
        test_from = 10
        test_sort = [{"timestamp": {"order": "desc"}}]

        mock_hits = [
            {"_source": {"field1": "value1", "field2": "value2"}},
            {"_source": {"field1": "value3", "field2": "value4"}},
        ]
        mock_response = {"hits": {"hits": mock_hits, "total": {"value": 2}}}

        self.mock_es_client.search.return_value = mock_response

        result = await self.async_es_dao.search(test_query, test_size, test_from, test_sort)

        expected_sources = [hit["_source"] for hit in mock_hits]
        self.assertEqual(result, expected_sources)

        expected_body = {"query": test_query, "size": test_size, "from": test_from, "sort": test_sort}
        self.mock_es_client.search.assert_called_once_with(index="test_index", body=expected_body)

    @pytest.mark.asyncio
    async def test_search_no_sort(self):
        """Test search without sort parameter"""
        test_query = {"match_all": {}}

        mock_response = {"hits": {"hits": []}}
        self.mock_es_client.search.return_value = mock_response

        result = await self.async_es_dao.search(test_query)

        self.assertEqual(result, [])

        expected_body = {"query": test_query, "size": 10, "from": 0}  # default  # default
        self.mock_es_client.search.assert_called_once_with(index="test_index", body=expected_body)

    @pytest.mark.asyncio
    async def test_search_missing_hits(self):
        """Test search when response has no hits structure"""
        test_query = {"match_all": {}}
        mock_response = {}  # No hits structure

        self.mock_es_client.search.return_value = mock_response

        result = await self.async_es_dao.search(test_query)

        self.assertEqual(result, [])

    @pytest.mark.asyncio
    async def test_search_failure(self):
        """Test search operation failure"""
        test_query = {"match": {"field1": "value1"}}

        self.mock_es_client.search.side_effect = ElasticsearchException("Search failed")

        with self.assertRaises(ElasticsearchException):
            await self.async_es_dao.search(test_query)

    @pytest.mark.asyncio
    async def test_search_raw_success(self):
        """Test successful search_raw operation returning full response"""
        test_body = {
            "size": 0,
            "query": {"wildcard": {"labels": "*environment*"}},
            "aggs": {"matching_values": {"terms": {"field": "labels", "include": ".*environment.*"}}},
        }

        mock_response = {
            "hits": {"hits": [], "total": {"value": 0}},
            "aggregations": {
                "matching_values": {
                    "buckets": [
                        {"key": "environment:production", "doc_count": 10},
                        {"key": "environment:staging", "doc_count": 5},
                    ]
                }
            },
        }

        self.mock_es_client.search.return_value = mock_response

        result = await self.async_es_dao.search_raw(test_body)

        # Verify the full response is returned
        self.assertEqual(result, mock_response)
        self.assertIn("aggregations", result)
        self.assertEqual(len(result["aggregations"]["matching_values"]["buckets"]), 2)

        # Verify ES client was called with correct parameters
        self.mock_es_client.search.assert_called_once_with(index="test_index", body=test_body)

    @pytest.mark.asyncio
    async def test_search_raw_failure(self):
        """Test search_raw operation failure"""
        test_body = {"query": {"match_all": {}}}

        self.mock_es_client.search.side_effect = ElasticsearchException("Search failed")

        with self.assertRaises(ElasticsearchException):
            await self.async_es_dao.search_raw(test_body)

    @pytest.mark.asyncio
    async def test_close_success(self):
        """Test successful client closure"""
        await self.async_es_dao.close()

        self.mock_es_client.close.assert_called_once()

    @pytest.mark.asyncio
    async def test_close_failure(self):
        """Test client closure with exception (should be handled gracefully)"""
        self.mock_es_client.close.side_effect = Exception("Close error")

        # Should not raise exception
        await self.async_es_dao.close()

        self.mock_es_client.close.assert_called_once()

    @pytest.mark.asyncio
    async def test_context_manager(self):
        """Test async context manager functionality"""
        async with self.async_es_dao as dao:
            self.assertIsInstance(dao, AsyncEsDao)
            self.assertEqual(dao, self.async_es_dao)

        # Close should be called when exiting context
        self.mock_es_client.close.assert_called_once()

    @pytest.mark.asyncio
    async def test_context_manager_with_exception(self):
        """Test async context manager when exception occurs"""
        with self.assertRaises(ValueError):
            async with self.async_es_dao as dao:
                raise ValueError("Test exception")

        # Close should still be called even with exception
        self.mock_es_client.close.assert_called_once()

    @patch("compute_core.dao.async_es_dao.Config")
    @patch("compute_core.dao.async_es_dao.AsyncElasticsearch")
    def test_init_missing_index_config(self, mock_async_es, mock_config):
        """Test initialization with missing index configuration"""
        mock_config.return_value.es_config.return_value = {
            "host": "localhost:9200",
            "username": "test_user",
            "password": "test_pass",
            # Missing 'index' key
        }

        with self.assertRaises(ValueError) as context:
            AsyncEsDao(env="test")

        self.assertIn("Elasticsearch index configuration is required", str(context.exception))

    @patch("compute_core.dao.async_es_dao.Config")
    @patch("compute_core.dao.async_es_dao.AsyncElasticsearch")
    def test_init_with_custom_env(self, mock_async_es, mock_config):
        """Test initialization with custom environment"""
        test_env = "production"
        mock_config.return_value.es_config.return_value = {
            "host": "prod.elasticsearch.com:9200",
            "username": "prod_user",
            "password": "prod_pass",
            "index": "prod_index",
        }

        mock_es_client = AsyncMock()
        mock_async_es.return_value = mock_es_client

        dao = AsyncEsDao(env=test_env)

        # Verify Config was called with the custom environment
        mock_config.assert_called_with(test_env)

        # Verify AsyncElasticsearch was initialized correctly
        mock_async_es.assert_called_with("prod.elasticsearch.com:9200", http_auth=("prod_user", "prod_pass"))

        self.assertEqual(dao.index, "prod_index")
        self.assertEqual(dao.es_client, mock_es_client)
