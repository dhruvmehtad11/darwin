from dataclasses import dataclass
from dataclasses_json import DataClassJsonMixin


@dataclass
class RayStartParams(DataClassJsonMixin):
    """
    Ray start parameters.
    Args:
        object_store_memory_perc: The percentage of memory(in bytes) to start the object store with.
                             By default, this is 25% of the total memory on every node.
                             Min value: 100MB and Max Value: 93% of total memory.
        num_cpus_on_head: the number of CPUs on head node
        num_gpus_on_head: the number of GPUs on head node
    """

    object_store_memory_perc: int = 25
    num_cpus_on_head: int = 0
    num_gpus_on_head: int = 0

    def __post_init__(self):
        if not isinstance(self.object_store_memory_perc, int):
            raise TypeError("object_store_memory_perc must be int")
        if not isinstance(self.num_cpus_on_head, int):
            raise TypeError("num_cpus_on_head must be int")
        if not isinstance(self.num_gpus_on_head, int):
            raise TypeError("num_gpus_on_head must be int")
        if self.object_store_memory_perc <= 0 or self.object_store_memory_perc > 93:
            raise ValueError(f"object_store_memory_perc {self.object_store_memory_perc} is not a valid input")
        if self.num_cpus_on_head < 0:
            raise ValueError(f"num_cpus_on_head {self.num_cpus_on_head} is not a valid input")
        if self.num_gpus_on_head < 0:
            raise ValueError(f"num_gpus_on_head {self.num_gpus_on_head} is not a valid input")

    def convert(self):
        return {
            "object_store_memory": self.object_store_memory_perc,
            "cpus_on_head": self.num_cpus_on_head,
        }
