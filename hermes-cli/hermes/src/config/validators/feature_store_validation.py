from typing import Dict, Any, List

from hermes.src.utils.api_caller_utils import call_api_endpoint
from hermes.src.config.config import Config


def get_feature_group_schema(feature_group_name: str, version: str) -> Dict[str, Any]:
    """
    Get the schema for a feature group.

    Args:
        feature_group_name (str): Name of the feature group

    Returns:
        Dict[str, Any]: Schema information for the feature group
    """
    config = Config()
    url = f"{config.get_feature_store_url}/feature-group/schema/"
    params = {"name": feature_group_name, "version": version}

    return call_api_endpoint(url=url, method="GET", params=params)


def validate_feature_group_schema(
    feature_group: List[Dict[str, Any]],
) -> None:
    """
    Validates feature group schema against the feature store.

    Args:
        feature_store_yaml_component (List[Dict[str, Any]]): List of feature group configurations

    Raises:
        ValueError: If schema validation fails or required keys are missing
    """
    if not feature_group:
        return

    required_keys = {"name", "features"}

    # Validate required keys exist
    missing_keys = required_keys - set(feature_group.keys())

    if missing_keys:
        raise ValueError(f"Missing required keys in feature group configuration: {missing_keys}")

    feature_group_name = feature_group["name"]
    version = feature_group["version"]

    # Get schema from feature store
    try:
        schema_response = get_feature_group_schema(feature_group_name, version)
    except Exception as e:
        raise ValueError(f"Invalid feature group name: {feature_group_name}") from e

    # Extract schema data
    schema_data = schema_response.get("data", {})
    schema_primary_keys = schema_data.get("primaryKeys", [])
    schema_features = {feature["name"] for feature in schema_data.get("schema", [])}

    # Set primary keys
    feature_group["primary_keys"] = schema_primary_keys

    # Validate features
    yaml_features = set(feature_group["features"])
    extra_features = yaml_features - schema_features
    if extra_features:
        raise ValueError(f"Extra features in feature group '{feature_group_name}': {extra_features}")
