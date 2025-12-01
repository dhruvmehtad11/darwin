from dataclasses import dataclass
from typing import Dict, Any

from dataclasses_json import DataClassJsonMixin

from compute_model.constant.constants import DISK_TYPE


@dataclass
class Disk(DataClassJsonMixin):
    """
    Disk configuration for compute cluster nodes.
    Args:
        disk_type: Disk type
        disk_size: Disk size in GBs
    """

    disk_type: str
    disk_size: int

    def __post_init__(self):
        if not isinstance(self.disk_type, str):
            raise TypeError(f"disk_type {self.disk_type} is not a valid input")

        if self.disk_type not in DISK_TYPE:
            raise ValueError(f"disk_type {self.disk_type} is not a valid input")
        if self.disk_size <= 0:
            raise ValueError(f"disk_size {self.disk_size} is not a valid input")

    def convert(self) -> Dict[Any, Any]:
        """
        Convert Disk to app layer dict.
        Returns:
            Dict[Any, Any]: Dict representation of Disk
        """
        return {"disk_type": self.disk_type, "storage_size": self.disk_size}
