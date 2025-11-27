import importlib
from dataclasses import dataclass

from compute_model.policy_definition import PolicyDefinition
from compute_script.dto.abstract_factory import AbstractPolicyFactory
from compute_script.dto.abstract_policy import Policy

DEFAULT_BASE_PATH_OF_POLICIES = "compute_script.dto.last_usage_policy"


@dataclass
class DefaultPolicyFactory(AbstractPolicyFactory):
    """
    Factory which creates the Policy using reflections.
    base_path is the path of the base folder which contains all the Policy Implementations.
    """

    base_path: str = DEFAULT_BASE_PATH_OF_POLICIES

    def get_policy(self, policy_def: PolicyDefinition) -> Policy:
        mod = importlib.import_module(self.base_path)
        policy_class = getattr(mod, policy_def.policy_name)(**policy_def.params)
        policy_class.set_enabled(policy_def.enabled)
        return policy_class
