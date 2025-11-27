import json
from unittest import IsolatedAsyncioTestCase
from unittest.mock import patch, AsyncMock

from compute_app_layer.controllers.maven.get_maven_library import get_maven_library_controller
from compute_app_layer.models.request.maven_search import MavenRepository


class TestGetMavenLibraryControllerTestCase(IsolatedAsyncioTestCase):
    @patch("compute_core.service.maven_service.MavenService", new_callable=AsyncMock)
    def setUp(self, mock_maven):
        self.mock_maven = mock_maven.return_value

    async def test_get_maven_library_controller_success(self):
        self.mock_maven.get_library.return_value = "test"
        resp = await get_maven_library_controller(
            maven=self.mock_maven, repository=MavenRepository.CENTRAL, search="test", page_size=1, offset=0
        )

        resp = json.loads(resp.body)

        self.assertEqual(resp["data"], "test")

    async def test_get_maven_library_controller_failure(self):
        self.mock_maven.get_library.side_effect = Exception("error")
        resp = await get_maven_library_controller(
            maven=self.mock_maven, repository=MavenRepository.CENTRAL, search="test", page_size=1, offset=0
        )

        self.assertEqual(resp.status_code, 500)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")

    async def test_get_maven_library_controller_page_size_failure(self):
        resp = await get_maven_library_controller(
            maven=self.mock_maven, repository=MavenRepository.CENTRAL, search="test", page_size=201, offset=0
        )

        self.assertEqual(resp.status_code, 500)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
