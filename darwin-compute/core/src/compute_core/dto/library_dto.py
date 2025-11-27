from dataclasses import dataclass
from datetime import datetime
from enum import Enum
from typing import Optional

from dataclasses_json import DataClassJsonMixin


class LibraryType(Enum):
    JAR = "jar"
    WHL = "whl"
    ZIP = "zip"
    TAR = "tar"
    GZ = "gz"
    TXT = "txt"
    PYPI = "pypi"
    MAVEN_JAR = "maven_jar"


class LibrarySource(Enum):
    PYPI = "pypi"
    MAVEN = "maven"
    S3 = "s3"
    WORKSPACE = "workspace"


class LibraryStatus(Enum):
    CREATED = "created"
    RUNNING = "running"
    FAILED = "failed"
    SUCCESS = "success"
    UNINSTALL_PENDING = "uninstall_pending"


@dataclass
class LibraryDTO(DataClassJsonMixin):
    cluster_id: Optional[str] = None
    name: Optional[str] = None
    status: Optional[LibraryStatus] = None
    id: Optional[int] = None
    version: Optional[str] = None
    type: Optional[LibraryType] = None
    source: Optional[LibrarySource] = None
    path: Optional[str] = None
    metadata: Optional[dict] = None
    execution_id: Optional[str] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

    def to_dict_with_enum_values(self) -> dict:
        def convert_enum_values(d):
            for key, value in d.items():
                if isinstance(value, Enum):
                    d[key] = value.value
                elif isinstance(value, dict):
                    convert_enum_values(value)
            return d

        result = self.to_dict()
        return convert_enum_values(result)


@dataclass
class DeleteLibrariesResponseDTO(DataClassJsonMixin):
    cluster_id: str
    packages: list[dict]
