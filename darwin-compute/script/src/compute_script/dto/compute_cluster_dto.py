from dataclasses import dataclass


@dataclass
class Node:
    head_node: int = 0
    jupyter: int = 0
    worker_nodes: int = 0

    def __post_init__(self):
        self._validate_int(self.head_node, "head_node")
        self._validate_int(self.jupyter, "jupyter")
        self._validate_int(self.worker_nodes, "worker_nodes")

    @staticmethod
    def _validate_int(value: int, field_name: str):
        if not isinstance(value, int):
            raise TypeError(f"{field_name} {value} should be int")

    def get_active_nodes(self) -> int:
        return self.worker_nodes + self.head_node


@dataclass
class ComputeClusterResourcesRequired:
    worker_nodes: int = 0
    total_memory: int = 0

    @staticmethod
    def _validate_int(value: int, field_name: str):
        if not isinstance(value, int):
            raise TypeError(f"{field_name} {value} should be int")

    @staticmethod
    def _validate_non_negative(value: int, field_name: str):
        if value < 0:
            raise ValueError(f"{field_name} {value} should be greater than or equal to 0")

    def __post_init__(self):
        self._validate_int(self.worker_nodes, "worker_nodes")
        self._validate_int(self.total_memory, "total_memory")
        self._validate_non_negative(self.worker_nodes, "worker_nodes")
        self._validate_non_negative(self.total_memory, "total_memory")
