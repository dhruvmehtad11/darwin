from enum import Enum
from typing import Optional
from dataclasses import dataclass
from dataclasses_json import DataClassJsonMixin


class PackageSource(Enum):
    PYPI = "pypi"
    MAVEN = "maven"
    S3 = "s3"
    WORKSPACE = "workspace"


class MvnRepository(Enum):
    MAVEN = "maven"
    SPARK = "spark"


@dataclass
class MvnMetadata(DataClassJsonMixin):
    repository: MvnRepository
    exclusions: Optional[str] = None


@dataclass
class PackageDetails(DataClassJsonMixin):
    name: Optional[str] = None
    version: Optional[str] = None
    path: Optional[str] = None
    metadata: Optional[MvnMetadata] = None


@dataclass
class Package(DataClassJsonMixin):
    source: PackageSource
    body: PackageDetails
