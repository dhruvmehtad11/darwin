from compute_app_layer.models.request.library import InstallRequest
from compute_core.constant.config import Config
from compute_core.dto.library_dto import LibraryDTO, LibrarySource, LibraryType

from compute_core.service.workspace_service import WorkspaceService
from compute_core.util.package_management.abstract.abstract_package import AbstractPackage
from compute_core.util.package_management.utils import (
    get_file_name_from_url,
    get_command_func_for_file_path,
)


class Workspace(AbstractPackage):
    """class for managing workspace"""

    def __init__(self, library: InstallRequest):
        self._dto_setter(library)
        config = Config()
        self.workspace_package_bucket = config.get_workspace_packages_bucket
        self.workspace_service = WorkspaceService()
        self.path = self._dto.path

    def _dto_setter(self, library: InstallRequest):
        self._dto = LibraryDTO(
            name=library.body.path.split("/")[-1],
            source=LibrarySource.WORKSPACE,
            type=LibraryType(library.body.path.split(".")[-1]),
            path=library.body.path,
        )

    def get_installation_command(self):
        # upload the file from path to S3
        file_name = get_file_name_from_url(self.path)
        self.workspace_service.upload_file_to_s3(self.path, self.workspace_package_bucket, file_name)

        func = get_command_func_for_file_path(f"{self.workspace_package_bucket}/{file_name}")
        if func:
            return func(f"{self.workspace_package_bucket}/{file_name}")
        raise ValueError(f"Unsupported package type: {self.path}")
