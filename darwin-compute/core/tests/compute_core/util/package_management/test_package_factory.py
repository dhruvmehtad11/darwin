from unittest import TestCase

from compute_app_layer.models.request.library import InstallRequest, LibraryRequest
from compute_core.dto.library_dto import LibrarySource
from compute_core.util.package_management.package_factory import PackageFactory


class TestPackageFactory(TestCase):
    def test_get_package_for_python_package(self):
        library = InstallRequest(
            source=LibrarySource.PYPI.value,
            body=LibraryRequest(
                name="test",
                version="1.0.0",
            ),
        )

        self.assertEqual(PackageFactory.get_package(library).__class__.__name__, "PythonPackage")

    def test_get_package_for_maven_package(self):
        library = InstallRequest(
            source=LibrarySource.MAVEN.value,
            body=LibraryRequest(
                name="test",
                version="1.0.0",
                metadata={
                    "repository": "maven",
                },
            ),
        )

        self.assertEqual(PackageFactory.get_package(library).__class__.__name__, "MavenPackage")

    def test_get_package_for_s3_package(self):
        library = InstallRequest(
            source=LibrarySource.S3.value,
            body=LibraryRequest(name="test", version="1.0.0", path="s3://test/test.jar"),
        )

        self.assertEqual(PackageFactory.get_package(library).__class__.__name__, "S3Package")

    def test_get_package_for_workspace_package(self):
        library = InstallRequest(
            source=LibrarySource.WORKSPACE.value,
            body=LibraryRequest(name="test", version="1.0.0", path="fsx://test/test.jar"),
        )

        self.assertEqual(PackageFactory.get_package(library).__class__.__name__, "Workspace")
