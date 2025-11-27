from abc import ABC, abstractmethod
from dataclasses import dataclass
from typing import List

from compute_model.policy_definition import PolicyDefinition
from compute_script.dto.abstract_policy import Policy


@dataclass
class AbstractPolicyFactory(ABC):
    """
    Base Class for Policy Factory
    """

    @abstractmethod
    def get_policy(self, policy_def: PolicyDefinition) -> Policy:
        """
        Takes PolicyDefinition and maps it to the Policy
        """
        pass

    def get_policies(self, policies: List[PolicyDefinition]) -> List[Policy]:
        """
        Takes List of PolicyDefinition and maps it to the List of Policy
        """

        policies_list = []
        for policy in policies:
            policies_list.append(self.get_policy(policy))
        return policies_list
