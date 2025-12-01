from unittest import TestCase

from compute_app_layer.models.request.library import InstallRequest, LibraryRequest, MvnMetadata
from compute_core.constant.constants import JAVA_PACKAGE_DOWNLOAD_DIR
from compute_core.dto.library_dto import LibrarySource
from compute_core.util.package_management.impl.maven_package import MavenPackage


class TestMavenPackage(TestCase):
    def test_get_installation_command(self):
        artifact_name = "test:test"
        version = "1.0.0"
        library = InstallRequest(
            source=LibrarySource.MAVEN.value,
            body=LibraryRequest(
                name=artifact_name,
                version=version,
                metadata=MvnMetadata(repository="maven"),
            ),
        )

        expected = [
            f"mkdir -p {JAVA_PACKAGE_DOWNLOAD_DIR}",
            f"mvn dependency:copy -Dartifact={artifact_name}:{version} -DoutputDirectory={JAVA_PACKAGE_DOWNLOAD_DIR}",
        ]
        self.assertEqual(MavenPackage(library).get_installation_command(), expected)
