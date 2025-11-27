from dataclasses import dataclass, field
from typing import Dict, Any

from dataclasses_json import DataClassJsonMixin


@dataclass
class PolicyDefinition(DataClassJsonMixin):
    """
    Policy definition for Auto Termination Policies of compute cluster.
    Args:
        policy_name: Name of the policy,
        params: [Optional] Parameters for the policy,
        enabled: [Optional] Whether the policy is enabled or not
    """

    policy_name: str
    params: Dict[Any, Any] = field(default_factory=lambda: {})
    enabled: bool = True

    def __post_init__(self):
        if not isinstance(self.policy_name, str):
            raise TypeError("policy_name must be str")
        if not isinstance(self.params, dict):
            raise TypeError("params must be dict")
        if not isinstance(self.enabled, bool):
            raise TypeError("enabled must be bool")

    def convert(self) -> Dict[Any, Any]:
        """
        Convert PolicyDefinition to app layer dict.
        Returns:
            Dict[Any, Any]: Dict representation of PolicyDefinition
        """
        return {
            "policy_name": self.policy_name,
            "enabled": self.enabled,
            "params": self.params,
        }
