from abc import ABC, abstractmethod


class FeatureStoreInterface(ABC):
    @abstractmethod
    def fetch_feature_group_data(self, *args, **kwargs):
        """Retrieve a specific feature by name with any number of parameters."""
        pass
