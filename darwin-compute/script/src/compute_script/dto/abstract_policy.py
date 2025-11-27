from abc import ABC, abstractmethod
from dataclasses import dataclass, field

from compute_script.dto.cluster_info import ClusterInfo


@dataclass
class Policy(ABC):
    """
    Abstract Class for Policy
    """

    enabled: bool = field(init=False, repr=False, default=True)

    @property
    def policy_name(self):
        """
        Returns: Policy name of the policy
        """
        return self.__class__.__name__

    def set_enabled(self, value: bool):
        """
        For setting enabled parameter
        Returns: self
        """
        self.enabled = value

    @abstractmethod
    def apply(self, cluster_info: ClusterInfo) -> bool:
        """
        Returns: True/False
        """
        pass

    def apply_if_enabled(self, cluster_info: ClusterInfo) -> bool:
        """
        Apply policy only if enabled=True.
        If enabled=False, returns False
        """
        return self.enabled and self.apply(cluster_info)
