# Configuration schema definitions
from functools import cache
from typing import Dict, List

from hermes.src.config.validators.custom_validators import EmailValidator, FeatureGroupValidator

from hermes.src.config.enums import ProjectType, InferenceType, FieldType

from hermes.src.config.models.base import ConfigField, ConfigSection


@cache
class ConfigurationSchema:
    def __init__(self):
        self.sections = self._initialize_schemas()

    def _initialize_schemas(self) -> Dict[str, ConfigSection]:
        # Base repository section (always required)
        repository_fields = [
            ConfigField("name", str, required=True, description="Repository name"),
            ConfigField("description", str, required=True, description="Repository description"),
            ConfigField(
                "author", str, required=True, description="Repository author email address", validator=EmailValidator()
            ),
            ConfigField("output_path", str, required=True, description="Output path for generated code"),
            ConfigField("type", str, required=True, choices=ProjectType._value2member_map_, description="Project type"),
        ]

        # FastAPI specific model section
        model_fields_fastapi = [
            ConfigField("author", str, required=True, validator=EmailValidator()),
            ConfigField("flavor", str, required=True, choices=["sklearn", "pytorch", "tensorflow"]),
            ConfigField("path", str, required=True),
        ]

        common_schema_definition_dict = {
            "name": ConfigField("name", str, required=True),
            "type": ConfigField("type", str, required=True),
            "description": ConfigField("description", str, required=False),
            "required": ConfigField("required", bool, required=True, default=False),
        }

        # Example of schema with nested optional fields
        serve_endpoint_fields = [
            ConfigField(
                name="endpoints",
                type={
                    "endpoint_name": ConfigField("endpoint_name", str, required=True),
                    "method": ConfigField("method", str, required=True, choices=["GET", "POST"]),
                    "request_schema": ConfigField(
                        name="request_schema",
                        required=True,
                        type={
                            "path_params": ConfigField(
                                "path_params", type=common_schema_definition_dict, required=False, is_list=True
                            ),
                            "query_params": ConfigField(
                                "query_params", type=common_schema_definition_dict, required=False, is_list=True
                            ),
                            "headers": ConfigField(
                                "headers", type=common_schema_definition_dict, required=False, is_list=True
                            ),
                            "body": ConfigField(
                                "body", type=common_schema_definition_dict, required=False, is_list=True
                            ),
                        },
                    ),
                    "response_schema": ConfigField(
                        name="response_schema",
                        required=True,
                        type={
                            "body": ConfigField("body", type=common_schema_definition_dict, required=True, is_list=True)
                        },
                    ),
                },
                required=True,
                is_list=True,
            )
        ]
        api_client_fields = [
            ConfigField(
                name="endpoints",
                type={
                    "endpoint_name": ConfigField(name="endpoint_name", type=str, required=True),
                    "url": ConfigField(name="url", type=str, required=True),
                    "path": ConfigField(name="path", type=str, required=True),
                    "method": ConfigField(name="method", type=str, required=True, choices=["GET", "POST"]),
                    "request_schema": ConfigField(
                        name="request_schema",
                        required=False,
                        type={
                            "path_params": ConfigField(
                                "path_params", type=common_schema_definition_dict, required=False, is_list=True
                            ),
                            "query_params": ConfigField(
                                "query_params", type=common_schema_definition_dict, required=False, is_list=True
                            ),
                            "headers": ConfigField(
                                "headers", type=common_schema_definition_dict, required=False, is_list=True
                            ),
                            "body": ConfigField(
                                "body", type=common_schema_definition_dict, required=False, is_list=True
                            ),
                        },
                    ),
                    "response_schema": ConfigField(
                        name="response_schema",
                        required=False,
                        type={
                            "body": ConfigField("body", type=common_schema_definition_dict, required=True, is_list=True)
                        },
                    ),
                },
                required=True,
                is_list=True,
            )
        ]

        feature_store_fields = [
            ConfigField(
                name="feature_groups",
                type={
                    "name": ConfigField("name", str, required=True),
                    "version": ConfigField("version", str, required=False),
                    "primary_keys": ConfigField("primary_keys", str, required=False, is_list=True),
                    "features": ConfigField("features", str, required=True, is_list=True),
                },
                required=True,
                is_list=True,
                validator=FeatureGroupValidator(),
            )
        ]

        runtime_environment_fields = [
            ConfigField(
                "dependencies", str, description="Name of the runtime environment", required=False, is_list=True
            ),
        ]

        return {
            "repository": ConfigSection("repository", repository_fields, required=True),
            "model_fastapi": ConfigSection(
                "model", model_fields_fastapi, required=False, conditional_on={"repository.type": "api"}
            ),
            "serve_endpoint_schemas": ConfigSection(
                "serve_endpoint_schemas",
                serve_endpoint_fields,
                required=True,
                conditional_on={"repository.type": "api"},
            ),
            "feature_store": ConfigSection("feature_store", feature_store_fields, required=False),
            "api_client": ConfigSection(
                "api_client", api_client_fields, required=False, conditional_on={"repository.type": "api"}
            ),
            "runtime_environment": ConfigSection("runtime_environment", runtime_environment_fields, required=False),
        }
