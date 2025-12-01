from compute_app_layer.models.request.library import InstallRequest
from compute_core.dto.library_dto import LibraryDTO, LibrarySource, LibraryType
from compute_core.util.package_management.abstract.abstract_package import AbstractPackage
from compute_core.util.package_management.utils import get_command_func_for_file_path


class S3Package(AbstractPackage):
    """class for managing packages from s3"""

    def __init__(self, library: InstallRequest):
        self._dto_setter(library)
        self.path = self._dto.path

    def _dto_setter(self, library: InstallRequest):
        self._dto = LibraryDTO(
            name=library.body.path.split("/")[-1],
            source=LibrarySource.S3,
            type=LibraryType(library.body.path.split(".")[-1]),
            path=library.body.path,
        )

    def get_installation_command(self):
        func = get_command_func_for_file_path(self.path)
        if func:
            return func(self.path)
        raise ValueError(f"Unsupported package type: {self.path}")
