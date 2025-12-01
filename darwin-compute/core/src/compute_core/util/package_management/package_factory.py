from compute_app_layer.models.request.library import InstallRequest
from compute_core.dto.library_dto import LibrarySource
from compute_core.util.package_management.abstract.abstract_package import AbstractPackage
from compute_core.util.package_management.impl.maven_package import MavenPackage
from compute_core.util.package_management.impl.python_package import PythonPackage
from compute_core.util.package_management.impl.s3_package import S3Package
from compute_core.util.package_management.impl.workspace import Workspace


class PackageFactory:
    """Factory class to return the appropriate package management class based on the library source"""

    @staticmethod
    def get_package(library: InstallRequest) -> AbstractPackage:
        if library.source.value == LibrarySource.WORKSPACE.value:
            return Workspace(library)
        elif library.source.value == LibrarySource.MAVEN.value:
            return MavenPackage(library)
        elif library.source.value == LibrarySource.PYPI.value:
            return PythonPackage(library)
        elif library.source.value == LibrarySource.S3.value:
            return S3Package(library)
        else:
            raise ValueError(f"Unsupported package source: {library.source}")
