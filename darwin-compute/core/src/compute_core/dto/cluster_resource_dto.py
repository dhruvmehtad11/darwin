from dataclasses import dataclass

from dataclasses_json import DataClassJsonMixin


@dataclass
class RayClusterResourceDTO:
    cores_used: int = 0
    memory_used: int = 0


@dataclass
class ResourceCapacity:
    usage_in_per: int = 0
    total: int = 0


@dataclass
class RayClusterResourceStats:
    cpu: ResourceCapacity = ResourceCapacity()
    memory: ResourceCapacity = ResourceCapacity()


@dataclass
class ClusterResourceDTO(DataClassJsonMixin):
    Name: str
    Type: str
    Status: str
    Message: str
