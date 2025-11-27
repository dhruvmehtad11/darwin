from typing import List, Dict, Union
import yaml
from .constants import required_fields
from .validations import (
    validate_inference_store,
    validate_api_client,
    validate_serve_endpoint_schemas,
    validate_runtime_environment,
    validate_repository_config,
    validate_feature_store,
    validate_model,
)
from hermes.src.utils.template_utils import prompt_with_default, prompt


def get_validated_yaml_config(yaml_file_path: str) -> Dict:
    """
    Validates the YAML configuration file for required fields.
    Returns a list of missing required fields.
    """
    flattened_context = {}
    missing_fields = []
    # Load YAML file if provided
    if yaml_file_path:
        context = load_yaml(yaml_file_path)
        flattened_context = flatten_dict(context)

    for field in required_fields:
        if field not in flattened_context:
            missing_fields.append(field)

    if missing_fields:
        responses = prompt_user(missing_fields)
        flattened_context.update(responses)

    validate_context(flattened_context)

    return flattened_context


def load_yaml(yaml_file):
    """Load YAML file and return its content as a dictionary."""
    with open(yaml_file, "r") as file:
        return yaml.safe_load(file)


def flatten_dict(dictionary, parent_key="", sep="_"):
    """Flatten nested dictionary with custom separator and handle special cases."""
    items = []
    # Special handling for repository fields
    if "repository" in dictionary:
        repo = dictionary["repository"]
        items.extend(
            [
                ("repository_name", repo.get("name")),
                ("repository_description", repo.get("description")),
                ("repository_author", repo.get("author")),
                ("repository_type", repo.get("type")),
                ("repository_inference_type", repo.get("inference_type")),
                ("repository_output_path", repo.get("output_path")),
            ]
        )

    if "feature_store" in dictionary:
        feature_store = dictionary["feature_store"]
        if "feature_groups" in feature_store:
            items.append(("feature_store", {"feature_group": feature_store["feature_groups"]}))

    if "inference_store" in dictionary:
        inference_store = dictionary["inference_store"]
        if "inference_groups" in inference_store:
            items.append(
                (
                    "inference_store",
                    {"inference_groups": inference_store["inference_groups"]},
                )
            )

    if "api_client" in dictionary:
        api_client = dictionary["api_client"]
        if "endpoints" in api_client:
            items.append(("api_client", {"endpoints": api_client["endpoints"]}))

    if "serve_endpoint_schemas" in dictionary:
        serve_endpoint_schemas = dictionary["serve_endpoint_schemas"]
        if serve_endpoint_schemas:
            items.append(("serve_endpoint_schemas", serve_endpoint_schemas))

    if "runtime_environment" in dictionary:
        deps = dictionary["runtime_environment"]
        if deps:
            items.append(("runtime_environment", deps))

    if "fastapi_serve_service" in dictionary:
        fastapi_serve_service = dictionary["fastapi_serve_service"]
        if fastapi_serve_service:
            items.append(("fastapi_serve_service", fastapi_serve_service))

    # Process remaining items normally
    for k, v in dictionary.items():
        if k not in [
            "repository",
            "feature_store",
            "api_client",
            "serve_endpoint_schemas",
            "runtime_environment",
            "inference_store",
            "fastapi_serve_service",
        ]:  # Skip already processed keys
            new_key = f"{parent_key}{sep}{k}" if parent_key else k
            if isinstance(v, dict):
                items.extend(flatten_dict(v, new_key, sep=sep).items())
            elif isinstance(v, list):
                items.append((new_key, v))
            else:
                items.append((new_key, v))

    return dict(items)


def prompt_user(missing_fields: List[str]) -> Dict:
    """Prompt user for configuration options that weren't provided in the YAML file"""

    responses = {}
    if "repository_inference_type" in missing_fields:
        responses["repository_inference_type"] = prompt_with_default(
            "Select inference type (online/offline/both) : ",
            "Invalid selection. Please choose 'online', 'offline' or 'both'",
            "offline",
            lambda x: x in ["online", "offline", "both"],
        )

    for field in missing_fields:
        if field == "repository_name":
            responses["repository_name"] = prompt(
                "Enter name of the repo to be created on local: ",
                "Repository name cannot be empty",
                lambda x: x.strip(),
            )

        elif field == "repository_author":
            responses["repository_author"] = prompt(
                "Enter email id of author: ",
                "Please enter a valid email address",
                lambda x: "@serve.com" in x,
            )

        elif field == "repository_type":
            responses["repository_type"] = prompt_with_default(
                "Select server type (fastapi/workflow/both) : ",
                "Invalid selection. Please choose 'fastapi', 'workflow', or 'both'",
                "fastapi",
                lambda x: x in ["fastapi", "workflow", "both"],
            )

        elif responses.get("repository_inference_type", "") in ["online", "both"]:
            if field == "model_path":
                responses["model_path"] = prompt(
                    "Enter MLflow model path (e.g., models:/my-model/production): ",
                    "MLflow model path cannot be empty",
                    lambda x: x.strip(),
                )

            elif field == "model_flavor":
                responses["model_flavor"] = prompt(
                    "Enter model flavor (e.g., sklearn,xgboost,pytorch,tensorflow,onnx): ",
                    "Model flavor cannot be empty",
                    lambda x: x.strip(),
                )

            elif field == "model_author":
                responses["model_author"] = prompt(
                    "Enter model author (e.g., user@serve.com): ",
                    "Model author cannot be empty",
                    lambda x: x.strip(),
                )

    return responses


def validate_context(context: dict):
    if "repository" in context:
        validate_repository_config(context["repository"])

    inference_type = context["repository_inference_type"]

    # Run feature store and model validations only for online or both
    if inference_type in ["online", "both"]:
        if "feature_store" in context:
            validate_feature_store(context["feature_store"])
        if "model" in context:
            validate_model(context["model"])

    # Run inference store validations only for offline or both
    if inference_type in ["offline", "both"]:
        if "inference_store" in context:
            validate_inference_store(context["inference_store"])

    # Always validate these regardless of inference type
    if "api_client" in context:
        validate_api_client(context["api_client"])
    if "serve_endpoint_schemas" in context:
        validate_serve_endpoint_schemas(context["serve_endpoint_schemas"])
    if "runtime_environment" in context:
        validate_runtime_environment(context["runtime_environment"])
