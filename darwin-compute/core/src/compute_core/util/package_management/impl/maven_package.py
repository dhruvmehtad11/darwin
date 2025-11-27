from compute_app_layer.models.request.library import InstallRequest
from compute_core.constant.constants import JAVA_PACKAGE_DOWNLOAD_DIR
from compute_core.dto.library_dto import LibraryDTO, LibrarySource, LibraryType
from compute_core.util.package_management.abstract.abstract_package import AbstractPackage


class MavenPackage(AbstractPackage):
    """Class for Maven package management"""

    def __init__(self, library: InstallRequest):
        self._dto_setter(library)
        self.artifact_name = self._dto.name
        self.version = self._dto.version

    def _dto_setter(self, library: InstallRequest):
        self._dto = LibraryDTO(
            name=library.body.name,
            version=library.body.version,
            source=LibrarySource.MAVEN,
            type=LibraryType.MAVEN_JAR,
            metadata=library.body.metadata.dict(),
        )

    def get_installation_command(self):
        return [
            f"mkdir -p {JAVA_PACKAGE_DOWNLOAD_DIR}",
            f"mvn dependency:copy -Dartifact={self.artifact_name}:{self.version} -DoutputDirectory={JAVA_PACKAGE_DOWNLOAD_DIR}",
        ]
