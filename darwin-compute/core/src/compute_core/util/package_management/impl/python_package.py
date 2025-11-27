from compute_app_layer.models.request.library import InstallRequest
from compute_core.dto.library_dto import LibraryDTO, LibrarySource, LibraryType
from compute_core.util.package_management.abstract.abstract_package import AbstractPackage
from compute_core.util.package_management.utils import get_trusted_host_from_index_url


class PythonPackage(AbstractPackage):
    """Class for Python package management"""

    def __init__(self, library: InstallRequest):
        self._dto_setter(library)
        self.name = self._dto.name
        self.version = self._dto.version
        self.index_url = self._dto.path

    def _dto_setter(self, library: InstallRequest):
        self._dto = LibraryDTO(
            name=library.body.name,
            version=library.body.version,
            source=LibrarySource.PYPI,
            type=LibraryType.WHL if library.body.name.endswith(".whl") else LibraryType.PYPI,
            path=library.body.path,
        )

    def get_installation_command(self):
        if self.version and self.index_url:
            return [
                f"pip install {self.name}=={self.version} --index-url {self.index_url} --trusted-host {get_trusted_host_from_index_url(self.index_url)}"
            ]
        elif self.version:
            return [f"pip install {self.name}=={self.version}"]
        elif self.index_url:
            return [
                f"pip install {self.name} --index-url {self.index_url} --trusted-host {get_trusted_host_from_index_url(self.index_url)}"
            ]
        else:
            return [f"pip install {self.name}"]
