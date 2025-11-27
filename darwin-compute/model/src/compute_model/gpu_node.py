from dataclasses import dataclass, field
from enum import Enum

from dataclasses_json import DataClassJsonMixin


@dataclass
class GPUNode(DataClassJsonMixin):
    """
    GPU node configuration for compute request.
    """

    name: str
    cores: int
    memory: int
    gpu_count: int
    g_ram_memory: int
    g_ram_type: str
    node_type: str = field(init=False, default="gpu")

    def __post_init__(self):
        if not isinstance(self.name, str):
            raise TypeError("name must be str")
        if not isinstance(self.cores, int):
            raise TypeError("head_node_cores must be int")
        if not isinstance(self.memory, int):
            raise TypeError("head_node_memory must be int")
        if not isinstance(self.gpu_count, int):
            raise TypeError("gpu_count must be int")
        if not isinstance(self.g_ram_memory, int):
            raise TypeError("g_ram_memory must be int")
        if not isinstance(self.g_ram_type, str):
            raise TypeError("g_ram_type must be str")

        self.verify()

    def verify(self):
        if self.to_dict() not in GPU_NODES_SPEC:
            raise ValueError(f"gpu node {self} is not provided by the platform")

    def convert(self):
        """
        Convert GPUNode to app layer dict.
        Returns:
            Dict[Any, Any]: Dict representation of GPUNode
        """
        return {
            "name": self.name,
            "cores": self.cores,
            "memory": self.memory,
            "gpu_count": self.gpu_count,
            "g_ram_memory": self.g_ram_memory,
            "g_ram_type": self.g_ram_type,
        }


class GPUNodeEnum(Enum):
    """
    Enum for GPU node types.
    """

    NVIDIA_T4 = "NVIDIA T4"
    NVIDIA_A10 = "NVIDIA A10"
    NVIDIA_A100_40GB = "NVIDIA A100 40GB"
    NVIDIA_A100_80GB = "NVIDIA A100 80GB"


GPU_NODES_SPEC = [
    {
        "name": GPUNodeEnum.NVIDIA_T4.value,
        "cores": 6,
        "memory": 26,
        "gpu_count": 1,
        "g_ram_memory": 16,
        "g_ram_type": "GDDR6",
        "node_type": "gpu",
    },
    {
        "name": GPUNodeEnum.NVIDIA_T4.value,
        "cores": 40,
        "memory": 165,
        "gpu_count": 4,
        "g_ram_memory": 64,
        "g_ram_type": "GDDR6",
        "node_type": "gpu",
    },
    {
        "name": GPUNodeEnum.NVIDIA_A10.value,
        "cores": 6,
        "memory": 26,
        "gpu_count": 1,
        "g_ram_memory": 24,
        "g_ram_type": "GDDR6",
        "node_type": "gpu",
    },
    {
        "name": GPUNodeEnum.NVIDIA_A100_40GB.value,
        "cores": 72,
        "memory": 900,
        "gpu_count": 8,
        "g_ram_memory": 320,
        "g_ram_type": "HBM2e",
        "node_type": "gpu",
    },
    {
        "name": GPUNodeEnum.NVIDIA_A100_80GB.value,
        "cores": 160,
        "memory": 1800,
        "gpu_count": 8,
        "g_ram_memory": 640,
        "g_ram_type": "HBM2e",
        "node_type": "gpu",
    },
]
