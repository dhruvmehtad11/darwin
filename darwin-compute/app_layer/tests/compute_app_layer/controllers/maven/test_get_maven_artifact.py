import json
from unittest.async_case import IsolatedAsyncioTestCase
from unittest.mock import patch, AsyncMock


from compute_app_layer.controllers.maven.get_maven_artifact import get_maven_artifact_versions_controller
from compute_core.dto.maven_dto import MavenArtifacts


class TestGetMavenArtifactControllerTestCase(IsolatedAsyncioTestCase):
    @patch("compute_core.service.maven_service.MavenService", new_callable=AsyncMock)
    def setUp(self, mock_maven):
        self.mock_maven = mock_maven.return_value

    async def test_get_maven_artifact_versions_controller_success(self):
        self.mock_maven.get_maven_artifact_versions.return_value = MavenArtifacts(
            group_id="1", artifact_id="1", versions=["test"]
        )
        resp = await get_maven_artifact_versions_controller(
            maven=self.mock_maven, group_id="test_group_id", artifact_id="test_artifact_id"
        )
        resp = json.loads(resp.body)

        expected = {"group_id": "1", "artifact_id": "1", "versions": ["test"]}
        self.assertEqual(resp["data"], expected)
        self.assertEqual(resp["status"], "SUCCESS")

    async def test_get_maven_library_controller_failure(self):
        self.mock_maven.get_maven_artifact_versions.side_effect = Exception("error")
        resp = await get_maven_artifact_versions_controller(
            maven=self.mock_maven, group_id="test_group_id", artifact_id="test_artifact_id"
        )
        self.assertEqual(resp.status_code, 500)
        resp = json.loads(resp.body)
        self.assertEqual(resp["status"], "ERROR")
