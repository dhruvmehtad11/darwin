from typing import Optional

from pydantic import BaseModel

from compute_core.dto.library_dto import LibraryType, LibrarySource, LibraryDTO, LibraryStatus


class LibraryResponse(BaseModel):
    cluster_id: Optional[str]
    status: Optional[LibraryStatus]
    id: Optional[str]
    name: Optional[str]
    version: Optional[str]


class LibraryError(BaseModel):
    error_code: str
    error_message: str


class LibraryDetailsResponse(LibraryResponse):
    type: Optional[LibraryType]
    source: Optional[LibrarySource]
    path: Optional[str]
    content: Optional[str]
    error: Optional[LibraryError]


class LibraryListResponse(BaseModel):
    cluster_id: str
    result_size: int
    packages: list[LibraryDTO]
