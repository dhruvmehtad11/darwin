from typing import Dict, Any, List
from ..utils.api_caller_utils import call_api_endpoint
from hermes.src.config.config import Config


def get_inference_group_schema(inference_group_name: str, version: str) -> Dict[str, Any]:
    """
    Get the schema for a inference group.

    Args:
        inference_group_name (str): Name of the inference group

    Returns:
        Dict[str, Any]: Schema information for the inference group
    """
    config = Config()
    url = f"{config.get_feature_store_url}/feature-group/schema/"
    params = {"name": inference_group_name, "version": version}

    return call_api_endpoint(url=url, method="GET", params=params)


def validate_inference_group_schema(
    inference_store_yaml_component: List[Dict[str, Any]],
) -> None:
    """
    Validates inference group schema against the inference store.

    Args:
        inference_store_yaml_component (List[Dict[str, Any]]): List of inference group configurations

    Raises:
        ValueError: If schema validation fails or required keys are missing
    """
    if not inference_store_yaml_component:
        return

    required_keys = {"name", "primary_keys", "features"}

    for inference_group in inference_store_yaml_component:
        # Validate required keys exist
        missing_keys = required_keys - set(inference_group.keys())

        if missing_keys:
            raise ValueError(f"Missing required keys in inference group configuration: {missing_keys}")

        inference_group_name = inference_group["name"]
        version = inference_group["version"]

        # Get schema from feature store
        try:
            schema_response = get_inference_group_schema(inference_group_name, version)
        except Exception as e:
            raise ValueError(f"Invalid inference group name: {inference_group_name}") from e

        # Extract schema data
        schema_data = schema_response.get("data", {})
        schema_primary_keys = set(schema_data.get("primaryKeys", []))
        schema_features = {feature["name"] for feature in schema_data.get("schema", [])}

        # Validate primary keys
        yaml_primary_keys = set(inference_group["primary_keys"])
        if yaml_primary_keys != schema_primary_keys:
            raise ValueError(
                f"Primary key mismatch for inference group '{inference_group_name}'. "
                f"Expected: {schema_primary_keys}, Found: {yaml_primary_keys}"
            )

        # Validate features
        yaml_features = set(inference_group["features"])
        extra_features = yaml_features - schema_features
        if extra_features:
            raise ValueError(f"Extra features in inference group '{inference_group_name}': {extra_features}")
