from enum import Enum
from typing import Optional

from pydantic import BaseModel

from compute_core.dto.library_dto import LibrarySource, LibraryDTO


class MvnRepository(Enum):
    MAVEN = "maven"
    SPARK = "spark"


class MvnMetadata(BaseModel):
    repository: MvnRepository
    exclusions: Optional[str] = None


class LibraryRequest(BaseModel):
    name: Optional[str]
    path: Optional[str]
    version: Optional[str]
    metadata: Optional[MvnMetadata]

    @classmethod
    def from_library_dto(cls, library_dto: LibraryDTO):
        return cls(
            name=library_dto.name,
            path=library_dto.path,
            version=library_dto.version,
            metadata=MvnMetadata(**library_dto.metadata) if library_dto.metadata else None,
        )


class InstallRequest(BaseModel):
    source: LibrarySource
    body: LibraryRequest

    @classmethod
    def from_library_dto(cls, library_dto: LibraryDTO):
        return cls(
            source=library_dto.source,
            body=LibraryRequest.from_library_dto(library_dto),
        )


class InstallBatchRequest(BaseModel):
    packages: list[InstallRequest]

    @classmethod
    def from_library_dtos(cls, library_dtos: list[LibraryDTO]):
        return cls(packages=[InstallRequest.from_library_dto(library_dto) for library_dto in library_dtos])


class SearchLibraryRequest(BaseModel):
    cluster_id: str
    key: str = ""
    sort_by: str
    sort_order: str
    offset: int = 0
    page_size: int = 10


class UninstallLibrariesRequest(BaseModel):
    id: list[int]
