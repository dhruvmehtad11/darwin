from abc import ABC, abstractmethod

from compute_app_layer.models.request.library import InstallRequest
from compute_core.dto.library_dto import LibraryDTO


class AbstractPackage(ABC):
    """Abstract class for package management"""

    _dto: LibraryDTO

    @property
    def dto(self):
        return self._dto

    @abstractmethod
    def _dto_setter(self, library: InstallRequest):
        pass

    @abstractmethod
    def get_installation_command(self) -> list[str]:
        pass
