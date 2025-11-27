from dataclasses import dataclass
from typing import Optional, Union

from dataclasses_json import DataClassJsonMixin

from compute_model.constant.constants import NODE_TYPE
from compute_model.cpu_node import CPUNode
from compute_model.gpu_node import GPUNode


@dataclass
class HeadNode(DataClassJsonMixin):
    """
    Head node configuration for compute request.
    """

    node: Union[CPUNode, GPUNode] = None
    node_type: Optional[str] = None

    def __init__(self, node: dict, node_type: Optional[str] = None):
        if node_type is not None and not isinstance(node_type, str):
            raise TypeError("node_type must be str")
        if node_type is not None and node_type not in NODE_TYPE:
            raise ValueError(f"node_type = {node_type} is not a valid input")
        self.node_type = node_type
        if self.node_type == "gpu":
            self.node = GPUNode.from_dict(node)
            self.node.verify()
        else:
            self.node = CPUNode.from_dict(node)
        self.node.node_type = self.node_type

    def convert(self) -> dict:
        """
        Convert HeadNode to app layer dict.
        Returns:
            Dict[Any, Any]: Dict representation of HeadNode
        """
        if self.node_type == "gpu":
            return {
                "cores": self.node.cores,
                "memory": self.node.memory,
                "node_type": self.node_type,
                "gpu_pod": self.node.convert(),
            }
        else:
            return {
                "cores": self.node.cores,
                "memory": self.node.memory,
                "node_type": self.node.node_type,
                "disk_setting": (self.node.disk.convert() if self.node.disk is not None else None),
                "node_capacity_type": (self.node.node_capacity_type if self.node.node_capacity_type else "ondemand"),
            }
