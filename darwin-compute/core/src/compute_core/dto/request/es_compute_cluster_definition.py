from dataclasses import dataclass, field
from typing import Optional

from dataclasses_json import DataClassJsonMixin

from compute_model.compute_cluster import ComputeClusterDefinition


@dataclass
class ESComputeDefinition(ComputeClusterDefinition, DataClassJsonMixin):
    status: str = "inactive"
    active_pods: int = 0
    last_used: str = ""
    total_memory_in_gb: int = 0
    labels_flat: Optional[list[str]] = field(default=None)

    def __post_init__(self):
        # Call parent's __post_init__ first
        super().__post_init__()

        if not isinstance(self.status, str):
            raise TypeError(f"status {self.status} is not a valid input")
        if not isinstance(self.active_pods, int):
            raise TypeError(f"active_pods {self.active_pods} is not a valid input")
        if not isinstance(self.last_used, str):
            raise TypeError(f"last_used {self.last_used} is not a valid input")
        if not isinstance(self.total_memory_in_gb, int):
            raise TypeError(f"total_memory_in_gb {self.total_memory_in_gb} is not a valid input")

        # Auto-populate labels_flat from labels
        self.labels_flat = self._generate_labels_flat()

    def _generate_labels_flat(self) -> list[str]:
        """Generate labels_flat array from labels dict for flattened-like search behavior"""
        if not self.labels or not isinstance(self.labels, dict):
            return []

        flat_labels = [v for k, v in self.labels.items()]
        return flat_labels
