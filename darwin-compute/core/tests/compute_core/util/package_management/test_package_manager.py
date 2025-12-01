import unittest
from unittest.mock import patch

import pytest

from compute_app_layer.models.request.library import (
    SearchLibraryRequest,
    InstallRequest,
    MvnMetadata,
    LibraryRequest,
    MvnRepository,
)
from compute_core.dto.library_dto import (
    LibraryDTO,
    LibrarySource,
    LibraryStatus,
    DeleteLibrariesResponseDTO,
    LibraryType,
)
from compute_core.util.package_management.package_manager import LibraryManager


class TestLibraryManager(unittest.TestCase):
    @pytest.fixture(autouse=True)
    def prepare_fixture(self, library_dto):
        self.library_details = [library_dto]

    @patch("compute_core.util.package_management.package_manager.PackageInstaller")
    @patch("compute_core.util.package_management.package_manager.RemoteCommand")
    @patch("compute_core.util.package_management.package_manager.LibraryDao")
    def setUp(self, mock_dao, mock_remote_command, mock_package_installer):
        self.mock_dao = mock_dao.return_value
        self.mock_remote_command = mock_remote_command.return_value
        self.mock_package_installer = mock_package_installer.return_value
        self.library_manager = LibraryManager()
        self.library_ids = [1, 2]

    def test_get_libraries(self):
        request = SearchLibraryRequest(
            key="test", cluster_id="test", sort_by="test", sort_order="test", offset=0, page_size=10
        )
        self.mock_dao.search.return_value = [
            LibraryDTO(
                cluster_id="test",
                name="test",
                source=LibrarySource.S3.value,
                status=LibraryStatus.SUCCESS,
                type=LibraryType.WHL,
                path="test",
            )
        ]
        resp = self.library_manager.get_libraries(request)
        self.assertIsInstance(resp[0], LibraryDTO)

    def test_install_library_workspace_inactive_cluster(self):
        library_dto = LibraryDTO(
            status=LibraryStatus.CREATED,
            type=LibraryType.WHL,
            source=LibrarySource.WORKSPACE,
            path="test",
            cluster_id="test",
            name="test",
            id=1,
            execution_id="test",
        )
        self.mock_package_installer.install.return_value = [library_dto]
        request = InstallRequest(source=LibrarySource.WORKSPACE, body=LibraryRequest(path="/test/test/test.whl"))
        self.mock_dao.add_library.return_value = [library_dto]
        resp = self.library_manager.add_library("cluster_id", "inactive", [request])
        self.assertIsInstance(resp[0], LibraryDTO)
        self.assertEqual(resp[0].status.value, "created")

    def test_install_library_workspace_non_inactive_cluster(self):
        library_dto = LibraryDTO(
            status=LibraryStatus.RUNNING,
            type=LibraryType.WHL,
            source=LibrarySource.WORKSPACE,
            path="test",
            cluster_id="test",
            name="test",
            id=1,
            execution_id="test",
        )
        self.mock_package_installer.install.return_value = [library_dto]
        request = InstallRequest(source=LibrarySource.WORKSPACE, body=LibraryRequest(path="/test/test/test.whl"))
        self.mock_dao.add_library.return_value = [library_dto]
        resp = self.library_manager.add_library("cluster_id", "active", [request])
        self.assertIsInstance(resp[0], LibraryDTO)
        self.assertEqual(resp[0].status.value, "running")

    def test_install_library_s3_inactive_cluster(self):
        library_dto = LibraryDTO(
            status=LibraryStatus.CREATED,
            type=LibraryType.WHL,
            source=LibrarySource.S3,
            path="test",
            cluster_id="test",
            name="test",
            id=1,
            execution_id="test",
        )
        self.mock_package_installer.install.return_value = [library_dto]
        request = InstallRequest(source=LibrarySource.S3, body=LibraryRequest(path="/test/test/test.whl"))
        self.mock_dao.add_library.return_value = [library_dto]
        resp = self.library_manager.add_library("cluster_id", "inactive", [request])
        self.assertIsInstance(resp[0], LibraryDTO)
        self.assertEqual(resp[0].status.value, "created")

    def test_install_library_s3_non_inactive_cluster(self):
        library_dto = LibraryDTO(
            status=LibraryStatus.RUNNING,
            type=LibraryType.WHL,
            source=LibrarySource.S3,
            path="test",
            cluster_id="test",
            name="test",
            id=1,
            execution_id="test",
        )
        self.mock_package_installer.install.return_value = [library_dto]
        request = InstallRequest(source=LibrarySource.S3, body=LibraryRequest(path="/test/test/test.whl"))
        self.mock_dao.add_library.return_value = [library_dto]
        resp = self.library_manager.add_library("cluster_id", "active", [request])
        self.assertIsInstance(resp[0], LibraryDTO)
        self.assertEqual(resp[0].status.value, "running")

    def test_install_library_pypi_inactive_cluster(self):
        library_dto = LibraryDTO(
            status=LibraryStatus.CREATED,
            type=LibraryType.PYPI,
            source=LibrarySource.PYPI,
            path="test",
            cluster_id="test",
            name="test",
            id=1,
            execution_id="test",
        )
        self.mock_package_installer.install.return_value = [library_dto]
        request = InstallRequest(
            source=LibrarySource.PYPI,
            body=LibraryRequest(name="test", version="test", path="test"),
        )
        self.mock_dao.add_library.return_value = [library_dto]
        resp = self.library_manager.add_library("cluster_id", "inactive", [request])
        self.assertEqual(resp[0].type.value, "pypi")
        self.assertIsInstance(resp[0], LibraryDTO)
        self.assertEqual(resp[0].status.value, "created")

    def test_install_library_pypi_non_inactive_cluster(self):
        library_dto = LibraryDTO(
            status=LibraryStatus.RUNNING,
            type=LibraryType.WHL,
            source=LibrarySource.PYPI,
            path="test",
            cluster_id="test",
            name="test",
            id=1,
            execution_id="test",
        )
        self.mock_package_installer.install.return_value = [library_dto]
        request = InstallRequest(
            source=LibrarySource.PYPI,
            body=LibraryRequest(name="test.whl", version="test", path="test"),
        )
        self.mock_dao.add_library.return_value = [library_dto]
        resp = self.library_manager.add_library("cluster_id", "active", [request])
        self.assertEqual(resp[0].type.value, "whl")
        self.assertIsInstance(resp[0], LibraryDTO)
        self.assertEqual(resp[0].status.value, "running")

    def test_install_library_maven_inactive_cluster(self):
        library_dto = LibraryDTO(
            status=LibraryStatus.CREATED,
            type=LibraryType.MAVEN_JAR,
            source=LibrarySource.MAVEN,
            path="test",
            cluster_id="test",
            name="test",
            id=1,
            execution_id="test",
        )
        self.mock_package_installer.install.return_value = [library_dto]
        request = InstallRequest(
            source=LibrarySource.MAVEN,
            body=LibraryRequest(name="test", version="test", metadata=MvnMetadata(repository="maven")),
        )
        self.mock_dao.add_library.return_value = [library_dto]
        resp = self.library_manager.add_library("cluster_id", "inactive", [request])
        self.assertIsInstance(resp[0], LibraryDTO)
        self.assertEqual(resp[0].status.value, "created")

    def test_install_library_maven_non_inactive_cluster(self):
        library_dto = LibraryDTO(
            status=LibraryStatus.RUNNING,
            type=LibraryType.MAVEN_JAR,
            source=LibrarySource.MAVEN,
            path="test",
            cluster_id="test",
            name="test",
            id=1,
            execution_id="test",
        )
        self.mock_package_installer.install.return_value = [library_dto]
        request = InstallRequest(
            source=LibrarySource.MAVEN.value,
            body=LibraryRequest(name="test", version="test", metadata=MvnMetadata(repository="maven")),
        )
        self.mock_dao.add_library.return_value = [library_dto]
        resp = self.library_manager.add_library("cluster_id", "active", [request])
        self.assertIsInstance(resp[0], LibraryDTO)
        self.assertEqual(resp[0].status.value, "running")

    def test_delete_cluster_library_success_cluster_inactive(self):
        self.mock_remote_command.delete_from_cluster.return_value = None
        self.mock_dao.delete_libraries.return_value = None

        resp = self.library_manager.delete_cluster_library(self.library_ids, "cluster_id", "inactive")

        expected_resp = DeleteLibrariesResponseDTO(
            cluster_id="cluster_id", packages=[{"id": library_id} for library_id in self.library_ids]
        )

        self.assertEqual(resp, expected_resp)

    def test_delete_cluster_library_success_cluster_not_inactive(self):
        self.mock_dao.update_libraries_status.return_value = 2

        resp = self.library_manager.delete_cluster_library(self.library_ids, "cluster_id", "active")

        expected = DeleteLibrariesResponseDTO(
            cluster_id="cluster_id", packages=[{"id": library_id} for library_id in self.library_ids]
        )

        self.assertEqual(resp, expected)

    def test_delete_cluster_library_with_delete_from_cluster_failure(self):
        self.mock_remote_command.delete_from_cluster.side_effect = Exception("error")

        with self.assertRaises(Exception) as context:
            self.library_manager.delete_cluster_library(self.library_ids, "cluster_id", "inactive")

    def test_delete_cluster_library_with_delete_libraries_from_library_table_failure(self):
        self.mock_dao.delete_libraries.side_effect = Exception("error")

        with self.assertRaises(Exception) as context:
            self.library_manager.delete_cluster_library(self.library_ids, "cluster_id", "inactive")

    def test_delete_cluster_library_with_update_status_of_library_having_id_failure(self):
        self.mock_dao.update_status_of_library_having_id.side_effect = Exception("error")

        with self.assertRaises(Exception) as context:
            self.library_manager.delete_cluster_library(self.library_ids, "cluster_id", "active")

    def test_get_unique_cluster_libraries(self):
        # S3 Path - Library already exists (should return 0 unique, 1 already installed)
        existing_s3_library = LibraryDTO(path="test", source=LibrarySource.S3)
        self.mock_dao.get_cluster_libraries.return_value = [existing_s3_library]
        request = [InstallRequest(source=LibrarySource.S3, body=LibraryRequest(path="test"))]
        unique_libs, already_installed_libs = self.library_manager.get_new_and_existing_libraries("cluster_id", request)
        self.assertEqual(len(unique_libs), 0)  # No unique libraries
        self.assertEqual(len(already_installed_libs), 1)  # One already installed
        self.assertIsInstance(already_installed_libs[0], LibraryDTO)  # Should be LibraryDTO
        self.assertEqual(already_installed_libs[0].path, "test")

        # Python Path - Different library exists (should return 1 unique, 0 already installed)
        self.mock_dao.get_cluster_libraries.return_value = [LibraryDTO(name="test", source=LibrarySource.PYPI)]
        unique_libs, already_installed_libs = self.library_manager.get_new_and_existing_libraries("cluster_id", request)
        self.assertEqual(len(unique_libs), 1)  # One unique library
        self.assertEqual(len(already_installed_libs), 0)  # No already installed

        # Maven Path - Library already exists (should return 0 unique, 1 already installed)
        existing_maven_library = LibraryDTO(
            name="test", version="test", metadata={"repository": "maven"}, source=LibrarySource.MAVEN
        )
        self.mock_dao.get_cluster_libraries.return_value = [existing_maven_library]
        request = [
            InstallRequest(
                source=LibrarySource.MAVEN,
                body=LibraryRequest(name="test", version="test", metadata=MvnMetadata(repository=MvnRepository.MAVEN)),
            )
        ]
        unique_libs, already_installed_libs = self.library_manager.get_new_and_existing_libraries("cluster_id", request)
        self.assertEqual(len(unique_libs), 0)  # No unique libraries
        self.assertEqual(len(already_installed_libs), 1)  # One already installed
        self.assertIsInstance(already_installed_libs[0], LibraryDTO)  # Should be LibraryDTO
        self.assertEqual(already_installed_libs[0].name, "test")
