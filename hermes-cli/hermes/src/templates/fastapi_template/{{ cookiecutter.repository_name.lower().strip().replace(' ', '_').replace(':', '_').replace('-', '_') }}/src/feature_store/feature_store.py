from typing import List, Dict, Any
from src.api_client.api_client import APIClient
from src.feature_store.feature_store_interface import FeatureStoreInterface
from src.config.config import Config


class FeatureStoreClient(FeatureStoreInterface):
    def __init__(self, api_client: APIClient, config: Config):
        self.api_client = api_client
        self.config = config
        self.feature_store_url = self.config.get_feature_store_url

    async def fetch_feature_group_data(
        self,
        feature_group_name: str,
        feature_columns: List[str],
        primary_key_names: List[str],
        primary_key_values: List[List[Any]],
    ) -> Dict[str, Any]:
        url = "/feature-group/read-features"
        payload = {
            "featureGroupName": feature_group_name,
            "featureColumns": feature_columns,
            "primaryKeys": {"names": primary_key_names, "values": primary_key_values},
        }
        headers = {"Content-Type": "application/json"}
        response = await self.api_client.get(url=url, base_url=self.feature_store_url, body=payload, headers=headers)
        return response
