from unittest import IsolatedAsyncioTestCase
from unittest.mock import patch, AsyncMock

from compute_app_layer.models.request.maven_search import MavenRepository
from compute_core.service.maven_service import MavenService


class TestMavenService(IsolatedAsyncioTestCase):

    @patch("compute_core.service.maven_service.make_async_api_request", new_callable=AsyncMock)
    async def test_get_library(self, mock_request):
        mock_response = {
            "response": {
                "docs": [
                    {"g": "com.example", "a": "artifact1", "latestVersion": "1.0.0"},
                    {"g": "com.example", "a": "artifact2", "latestVersion": "2.0.0"},
                ],
                "numFound": 2,
            }
        }

        repository = MavenRepository.CENTRAL
        search_query = "example"
        page_size = 10
        offset = 0
        mock_request.return_value = mock_response
        maven_service = MavenService()
        result = await maven_service.get_library(repository, search_query, page_size, offset)
        self.assertEqual(result.result_size, 2)
        self.assertEqual(len(result.packages), 2)

        self.assertEqual(result.packages[0]["artifact_id"], "artifact1")
        self.assertEqual(result.packages[1]["artifact_id"], "artifact2")

    @patch("compute_core.service.maven_service.make_async_api_request", new_callable=AsyncMock)
    async def test_get_library_error_handling(self, mock_request):
        mock_request.side_effect = Exception("API error occurred")

        repository = MavenRepository.CENTRAL
        search_query = "example"
        page_size = 10
        offset = 0
        maven_service = MavenService()
        with self.assertRaises(Exception):
            await maven_service.get_library(repository, search_query, page_size, offset)

    @patch("compute_core.service.maven_service.make_async_api_request", new_callable=AsyncMock)
    async def test_get_all_maven_artifact_version_response_success(self, mock_request):
        mock_response_1 = {
            "response": {
                "docs": [
                    {"g": "com.example", "a": "artifact1", "latestVersion": "1.0.0"},
                    {"g": "com.example", "a": "artifact2", "latestVersion": "2.0.0"},
                ],
                "numFound": 40,  # Simulate 40 results available
            }
        }

        mock_response_2 = {"response": {"docs": [], "numFound": 40}}

        # Set the side effect of _request to return different responses for two pages
        mock_request.side_effect = [mock_response_1, mock_response_2]

        method = "GET"
        url = "http://example.com/maven"
        query = "example"
        maven_service = MavenService()
        result = await maven_service.get_all_maven_artifact_version_response(method, url, query)
        self.assertEqual(len(result), 2)
        self.assertEqual(result[0]["a"], "artifact1")
        self.assertEqual(result[1]["a"], "artifact2")

    @patch("compute_core.service.maven_service.make_async_api_request", new_callable=AsyncMock)
    async def test_get_all_maven_artifact_version_response_error(self, mock_request):
        mock_request.side_effect = Exception("API error occurred")

        method = "GET"
        url = "http://example.com/maven"
        query = "example"
        maven_service = MavenService()
        with self.assertRaises(Exception):
            await maven_service.get_all_maven_artifact_version_response(method, url, query)

    @patch(
        "compute_core.service.maven_service.MavenService.get_all_maven_artifact_version_response",
        new_callable=AsyncMock,
    )
    async def test_get_maven_artifact_versions_success(self, mock_get_all_versions):
        mock_response = [
            {"g": "com.example", "a": "artifact1", "v": "1.0.0"},
            {"g": "com.example", "a": "artifact1", "v": "2.0.0"},
            {"g": "com.example", "a": "artifact1", "v": "3.0.0"},
        ]
        mock_get_all_versions.return_value = mock_response

        group_id = "com.example"
        artifact_id = "artifact1"
        maven_service = MavenService()
        result = await maven_service.get_maven_artifact_versions(group_id, artifact_id)

        self.assertEqual(result.group_id, group_id)
        self.assertEqual(result.artifact_id, artifact_id)
        self.assertEqual(result.versions, ["1.0.0", "2.0.0", "3.0.0"])

    @patch(
        "compute_core.service.maven_service.MavenService.get_all_maven_artifact_version_response",
        new_callable=AsyncMock,
    )
    async def test_get_maven_artifact_versions_error_handling(self, mock_get_all_versions):
        mock_get_all_versions.side_effect = Exception("API error occurred")

        group_id = "com.example"
        artifact_id = "artifact1"

        with self.assertRaises(Exception):
            await self.maven_service.get_maven_artifact_versions(group_id, artifact_id)
