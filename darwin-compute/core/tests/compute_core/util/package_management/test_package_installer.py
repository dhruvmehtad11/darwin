from unittest import TestCase
from unittest.mock import patch

from compute_app_layer.models.request.library import InstallRequest, LibraryRequest
from compute_core.dto.library_dto import LibrarySource
from compute_core.util.package_management.package_installer import PackageInstaller


class TestPackageInstaller(TestCase):
    @patch("compute_core.util.package_management.package_manager.RemoteCommand")
    def setUp(self, mock_remote_command):
        self.mock_remote_command = mock_remote_command.return_value
        self.install_requests = [
            InstallRequest(
                source=LibrarySource.PYPI.value,
                body=LibraryRequest(name="test", version="1.0.0"),
            ),
            InstallRequest(
                source=LibrarySource.MAVEN.value,
                body=LibraryRequest(name="test", version="1.0.0", metadata={"repository": "maven"}),
            ),
            InstallRequest(
                source=LibrarySource.S3.value,
                body=LibraryRequest(name="test", version="1.0.0", path="s3://test/test.jar"),
            ),
        ]

    def test_installation_for_inactive_cluster(self):
        self.mock_remote_command.add_to_cluster.return_value = {
            "remote_commands": [{"execution_id": "test1"}, {"execution_id": "test2"}, {"execution_id": "test3"}]
        }

        res = PackageInstaller(self.mock_remote_command).install(self.install_requests, "cluster_id", "inactive")
        self.assertEqual(len(res), 3)
        self.assertEqual(res[0].execution_id, "test1")
        self.assertEqual(res[1].execution_id, "test2")
        self.assertEqual(res[2].execution_id, "test3")

    def test_installation_for_active_cluster(self):
        self.mock_remote_command.execute_multiple_on_cluster.return_value = {
            "remote_commands": [{"execution_id": "test1"}, {"execution_id": "test2"}, {"execution_id": "test3"}]
        }

        res = PackageInstaller(self.mock_remote_command).install(self.install_requests, "cluster_id", "active")

        self.assertEqual(len(res), 3)
        self.assertEqual(res[0].execution_id, "test1")
        self.assertEqual(res[1].execution_id, "test2")
        self.assertEqual(res[2].execution_id, "test3")
