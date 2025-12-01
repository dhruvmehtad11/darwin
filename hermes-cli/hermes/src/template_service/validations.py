from typing import Dict, Any
from enum import Enum
from .inference_store_validation import validate_inference_group_schema
from hermes.src.config.validators.feature_store_validation import validate_feature_group_schema


class ValidPythonTypes(Enum):
    INTEGER = "int"
    FLOAT = "float"
    STRING = "str"
    BOOL = "bool"
    ARRAY = "array"
    DICT = "dict"
    LIST = "List"
    DICT_STR = "Dict"


def validate_type(type_str: str) -> bool:
    """Validate if the type string matches valid Python types"""
    # Handle generic types like List[str] or Dict[str,str]
    if "[" in type_str and "]" in type_str:
        base_type = type_str.split("[")[0]
        return base_type in [t.value for t in ValidPythonTypes]

    return type_str in [t.value for t in ValidPythonTypes]


def validate_inference_store(inference_store: dict):
    """Validate inference store configuration"""
    if not inference_store or not isinstance(inference_store, dict):
        return

    inference_groups = inference_store.get("inference_groups", [])
    if not isinstance(inference_groups, list):
        raise ValueError("inference_groups must be a list")

    for group in inference_groups:
        if not isinstance(group, dict):
            raise ValueError("Each inference group must be a dictionary")

        required_fields = ["name", "primary_keys", "features"]
        for field in required_fields:
            if field not in group:
                raise ValueError(f"Missing required field '{field}' in inference group")

        if not isinstance(group["primary_keys"], list):
            raise ValueError("primary_keys must be a list")

        if not isinstance(group["features"], list):
            raise ValueError("features must be a list")
    validate_inference_group_schema(inference_groups)


def validate_feature_store(feature_store: dict):
    """Validate feature store configuration"""
    if not feature_store or not isinstance(feature_store, dict):
        return

    feature_groups = feature_store.get("feature_groups", [])
    if not isinstance(feature_groups, list):
        raise ValueError("feature_groups must be a list")

    for group in feature_groups:
        if not isinstance(group, dict):
            raise ValueError("Each feature group must be a dictionary")

        required_fields = ["name", "primary_keys", "features"]
        for field in required_fields:
            if field not in group:
                raise ValueError(f"Missing required field '{field}' in feature group")

        if not isinstance(group["primary_keys"], list):
            raise ValueError("primary_keys must be a list")

        if not isinstance(group["features"], list):
            raise ValueError("features must be a list")
    validate_feature_group_schema(feature_groups)


def validate_api_client(api_client: dict):
    """Validate API client configuration"""
    if not api_client or not isinstance(api_client, dict):
        return

    if "endpoints" not in api_client:
        raise ValueError("Missing required 'endpoints' in api_client")

    endpoints = api_client["endpoints"]
    if not isinstance(endpoints, list):
        raise ValueError("endpoints must be a list")

    for endpoint in endpoints:
        if not isinstance(endpoint, dict):
            raise ValueError("Each endpoint must be a dictionary")

        for service_name, service_config in endpoint.items():
            required_fields = ["url", "path", "method"]
            for field in required_fields:
                if field not in service_config:
                    raise ValueError(f"Missing required field '{field}' in endpoint {service_name}")

            # Validate parameter types
            for param_group in ["query_params", "headers", "request_body"]:
                if param_group in service_config:
                    for param in service_config[param_group]:
                        for param_name, param_config in param.items():
                            if not validate_type(param_config["type"]):
                                raise ValueError(
                                    f"Invalid type '{param_config['type']}' in {param_group} for {service_name}"
                                )


def validate_serve_endpoint_schemas(serve_endpoint_schemas: dict):
    """Validate serve endpoint schemas configuration for REST API request/response schemas"""
    if not serve_endpoint_schemas or not isinstance(serve_endpoint_schemas, dict):
        return

    for schema_type in ["request_schema", "response_schema"]:
        if schema_type not in serve_endpoint_schemas:
            continue

        schema_list = serve_endpoint_schemas[schema_type]
        if not isinstance(schema_list, dict):
            raise ValueError(f"{schema_type} schema must be a dictionary")

        # Handle nested body structure
        for section, fields in schema_list.items():
            if not isinstance(fields, list):
                raise ValueError(f"Fields in {schema_type}.{section} must be a list")

            for field in fields:
                if not isinstance(field, dict):
                    raise ValueError(f"Each field in {schema_type}.{section} must be a dictionary")

                # Validate required fields
                required_fields = ["name", "type"]
                missing_fields = [f for f in required_fields if f not in field]
                if missing_fields:
                    raise ValueError(f"Missing required fields {missing_fields} in {schema_type}.{section} field")

                # Validate type
                if not validate_type(field["type"]):
                    raise ValueError(f"Invalid type '{field['type']}' in {schema_type}.{section} schema")

                # Validate description if present
                if "description" in field and not isinstance(field["description"], str):
                    raise ValueError(f"Description must be a string in {schema_type}.{section} schema")

                # Validate required flag if present
                if "required" in field and not isinstance(field["required"], bool):
                    raise ValueError(f"Required flag must be a boolean in {schema_type}.{section} schema")


def validate_runtime_environment(runtime_environment: dict):
    """Validate runtime environment configuration"""
    if not runtime_environment or not isinstance(runtime_environment, dict):
        return

    if "env_variables" in runtime_environment:
        if not isinstance(runtime_environment["env_variables"], list):
            raise ValueError("env_variables must be a list")

        for env_var in runtime_environment["env_variables"]:
            if not isinstance(env_var, dict):
                raise ValueError("Each environment variable must be a dictionary")

    if "dependencies" in runtime_environment:
        if not isinstance(runtime_environment["dependencies"], list):
            raise ValueError("dependencies must be a list")

        for dependency in runtime_environment["dependencies"]:
            if not isinstance(dependency, str):
                raise ValueError("Each dependency must be a string")


def validate_model(model: dict):
    """Validate model configuration"""
    if not model or not isinstance(model, dict):
        raise ValueError("Model configuration is required")

    required_fields = ["author", "flavor", "path"]
    for field in required_fields:
        if field not in model:
            raise ValueError(f"Missing required field '{field}' in model configuration")

    valid_flavors = ["sklearn", "xgboost", "pytorch", "tensorflow", "onnx"]
    if model["flavor"] not in valid_flavors:
        raise ValueError(f"Invalid model flavor. Must be one of: {', '.join(valid_flavors)}")

    if not model["path"].startswith("mlflow-artifacts:/"):
        raise ValueError("Model path must start with 'mlflow-artifacts:/'")


def validate_repository_config(repository: Dict[str, Any]) -> None:
    """Validate repository configuration"""
    required_fields = [
        "repository_name",
        "repository_author",
        "repository_type",
        "repository_inference_type",
    ]

    if not repository or not isinstance(repository, dict):
        raise ValueError("Repository configuration is required")

    missing_fields = [f for f in required_fields if f not in repository]
    if missing_fields:
        raise ValueError(f"Missing required repository fields: {missing_fields}")

    valid_types = ["fastapi", "workflow", "both"]
    if repository["type"] not in valid_types:
        raise ValueError(f"Invalid repository type. Must be one of: {', '.join(valid_types)}")

    valid_inference_types = ["online", "offline", "both"]
    if repository["inference_type"] not in valid_inference_types:
        raise ValueError(f"Invalid inference type. Must be one of: {', '.join(valid_inference_types)}")

    if not repository["author"].endswith("@serve.com"):
        raise ValueError("Author email must be a serve.com email address")


def validate_schema(schema: Dict[str, Any], schema_type: str) -> None:
    """Validate schema structure and types"""
    if not isinstance(schema, dict):
        raise ValueError(f"{schema_type} schema must be a dictionary")

    required_fields = ["name", "type"]
    for field in schema:
        missing_fields = [f for f in required_fields if f not in field]
        if missing_fields:
            raise ValueError(f"Missing required fields {missing_fields} in {schema_type} schema")

        if not validate_type(field["type"]):
            raise ValueError(f"Invalid type '{field['type']}' in {schema_type} schema")


def validate_model_class(model_class: Dict[str, Any]) -> None:
    """Validate model class configuration"""
    if not model_class or not isinstance(model_class, dict):
        return

    # Validate initialization section
    if "initialization" in model_class:
        init = model_class["initialization"]
        if "models" in init:
            for model in init["models"]:
                if "model_id" not in model:
                    raise ValueError("Missing model_id in initialization models")

    # Validate preprocessing schema
    if "preprocessing" in model_class:
        preproc = model_class["preprocessing"]
        if "schema" in preproc:
            schema = preproc["schema"]
            if "input" in schema:
                validate_schema(schema["input"], "preprocessing input")
            if "output" in schema:
                validate_schema(schema["output"], "preprocessing output")

    # Validate predict schema
    if "predict" in model_class:
        predict = model_class["predict"]
        if "schema" in predict:
            if "prediction" in predict["schema"]:
                validate_schema(predict["schema"]["prediction"], "prediction")

    # Validate post-processing schema
    if "post_processing" in model_class:
        postproc = model_class["post_processing"]
        if "schema" in postproc:
            if "output" in postproc["schema"]:
                validate_schema(postproc["schema"]["output"], "post-processing output")


def validate_defaults(defaults: Dict[str, Any]) -> None:
    """Validate defaults configuration"""
    if not defaults or not isinstance(defaults, dict):
        return

    if "request_input" in defaults:
        req_input = defaults["request_input"]
        if "schema" in req_input:
            for field in req_input["schema"]:
                if not all(k in field for k in ["name", "type", "value"]):
                    raise ValueError("Default request input schema must contain name, type, and value")
                if not validate_type(field["type"]):
                    raise ValueError(f"Invalid type '{field['type']}' in default request input")
