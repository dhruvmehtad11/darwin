from typing import List, Dict, Any

from src.api_client import APIClient
from src.feature_store.feature_store_interface import FeatureStoreInterface
from src.config.config import Config


class FeatureStoreClient(FeatureStoreInterface):
    def __init__(self, api_client: APIClient, config: Config):
        self.api_client = api_client
        self.config = config
        self.feature_store_url = self.config.get_feature_store_url
        self.ofs_admin_url = self.config.get_ofs_admin_url

    async def fetch_feature_group_data(
        self,
        feature_group_name: str,
        feature_columns: List[str],
        primary_key_names: List[str],
        primary_key_values: List[List[Any]],
    ):
        url = "/feature-group/read-features"
        payload = {
            "featureGroupName": feature_group_name,
            "featureColumns": feature_columns,
            "primaryKeys": {"names": primary_key_names, "values": primary_key_values},
        }
        headers = {"Content-Type": "application/json"}
        response = await self.api_client.get(
            url=url, base_url=self.feature_store_url, body=payload, headers=headers
        )
        return response["data"]["successfulKeys"][0]["features"]


    async def fetch_feature_meta_data(self, feature_group_name: str):
        url = "/feature-group/schema"
        payload = {"name": feature_group_name}
        headers = {"Content-Type": "application/json"}
        response = await self.api_client.get(
            url=url, base_url=self.ofs_admin_url, query_params=payload, headers=headers
        )

        # Handle both shapes: {"data": {...}} or {"body": {"data": {...}}}
        data = response.get("data", {})
        schema = data.get("schema", [])

        return [x["name"] for x in schema]
