import warnings
from pathlib import Path
from typing import Any, Dict, List, Union, Set

import questionary
import yaml

from hermes.src.config.schema import ConfigurationSchema
from hermes.src.config.models.base import ConfigField, ConfigSection


class ConfigValidationError(Exception):
    """Custom exception for configuration validation errors."""

    pass


class ConfigurationManager:
    def __init__(self):
        self.schema = ConfigurationSchema()
        self.config = {}

    def _get_list_input(self, field: ConfigField) -> List[Any]:
        items = []

        # For optional lists, first ask if they want to add any items
        if not field.required:
            if not questionary.confirm(f"Would you like to add any {field.name}?", default=False).ask():
                return []  # Return empty list instead of None

        while True:
            # If we have items and it's optional, ask if they want to add more
            if items and not questionary.confirm(f"Do you want to add another {field.name}?", default=True).ask():
                break

            if isinstance(field.type, dict):
                item = self._get_complex_list_item(field)
            else:
                item = self._get_input_for_field(ConfigField(name=f"{field.name} item", type=field.type, required=True))

            if item is not None:
                items.append(item)

        return items  # Always return the list, even if empty

    def _get_complex_list_item(self, schema: ConfigField) -> Dict[str, Any]:
        item = {}
        print(f"\nEntering details for new {schema.name}:")
        if isinstance(schema.type, dict):
            for key, field_schema in schema.type.items():
                value = self._get_input_for_field(field_schema)
                if value is not None:
                    item[key] = value

        return item

    def _get_input_for_field(self, field: ConfigField) -> Any:
        if field.is_list:
            return self._get_list_input(field)

        # For optional non-list fields
        if not field.required:
            if not questionary.confirm(f"Would you like to specify {field.name}?", default=False).ask():
                return None  # This will ensure the key exists with None value

        if field.choices:
            return questionary.select(
                f"{field.description or field.name}" + (" (optional)" if not field.required else ""),
                choices=field.choices,
            ).ask()

        if field.type == bool:
            return questionary.confirm(
                f"{field.description or field.name}" + (" (optional)" if not field.required else ""),
                default=field.default if field.default is not None else False,
            ).ask()

        if isinstance(field.type, dict):
            return self._get_complex_list_item(field)

        while True:
            value = questionary.text(
                f"{field.description or field.name}" + (" (optional)" if not field.required else ""),
                default=str(field.default) if field.default is not None else "",
            ).ask()

            if not field.required and not value:
                return None

            try:
                typed_value = field.type(value)
                if field.validator and not field.validator.validate(typed_value):
                    print(field.validator.get_error_message())
                    continue
                return typed_value
            except ValueError:
                print(f"Invalid input. Expected type: {field.type.__name__}")

    def _should_include_section(self, section: ConfigSection, current_config: Dict) -> bool:
        if not section.conditional_on:
            return True

        for condition_key, condition_value in section.conditional_on.items():
            keys = condition_key.split(".")
            config_value = current_config
            for key in keys:
                if key not in config_value:
                    return False
                config_value = config_value[key]

            if config_value != condition_value:
                return False

        return True

    def collect_interactive_input(self) -> Dict:
        for section_name, section in self.schema.sections.items():
            if not self._should_include_section(section, self.config):
                continue
            # import pdb; pdb.set_trace()
            if not section.required:
                include_section = questionary.confirm(f"Do you want to configure {section.name}?", default=False).ask()
                if not include_section:
                    continue
            section_config = {}
            print(f"\nConfiguring {section.name}:")

            for field in section.fields:
                value = self._get_input_for_field(field)
                section_config[field.name] = value

            # Add the section even if all fields are empty/None
            if section.required or any(v is not None for v in section_config.values()):
                self.config[section.name] = section_config

        return self.config

    def save_to_yaml(self, file_path: str):
        with open(file_path, "w") as file:
            yaml.dump(self.config, file, default_flow_style=False)

    def load_from_yaml(self, file_path: Union[str, Path]) -> Dict[str, Any]:
        """
        Load and validate a YAML configuration file against the schema.

        Args:
            file_path: Path to the YAML configuration file

        Returns:
            Dict containing the validated configuration

        Raises:
            ConfigValidationError: If validation fails
            yaml.YAMLError: If YAML parsing fails
        """
        # Load YAML file
        if not (file_path):
            return {}
        try:
            with open(file_path, "r") as f:
                config_data = yaml.safe_load(f)
        except yaml.YAMLError as e:
            raise ConfigValidationError(f"Failed to parse YAML file: {str(e)}")
        except FileNotFoundError:
            raise ConfigValidationError(f"Configuration file not found: {file_path}")

        # Validate the loaded configuration
        return self.validate_config(config_data)

    def _get_known_fields(self, section: ConfigSection) -> Set[str]:
        """Get set of known field names for a section."""
        return {field.name for field in section.fields}

    def _check_unknown_fields(self, section_name: str, section_data: Dict[str, Any], known_fields: Set[str]):
        """Check for and warn about unknown fields in the configuration."""
        unknown_fields = set(section_data.keys()) - known_fields
        if unknown_fields:
            warnings.warn(
                f"Unknown fields found in section '{section_name}' and will be ignored: {', '.join(unknown_fields)}",
                UserWarning,
            )

    def validate_config(self, config_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Validate configuration data against the schema.

        Args:
            config_data: Dictionary containing configuration data

        Returns:
            Dict containing the validated configuration with default values applied

        Raises:
            ConfigValidationError: If validation fails
        """
        if not isinstance(config_data, dict):
            raise ConfigValidationError("Configuration must be a dictionary")

        validated_config = {}
        known_sections = {section.name for section in self.schema.sections.values()}

        # Check for unknown sections
        unknown_sections = set(config_data.keys()) - known_sections
        if unknown_sections:

            warnings.warn(
                f"Unknown sections found in configuration and will be ignored: {', '.join(unknown_sections)}",
                UserWarning,
            )

        # Validate each section
        for section in self.schema.sections.values():
            # Check if section should be validated based on conditional
            if section.conditional_on:
                for key, value in section.conditional_on.items():
                    key_parts = key.split(".")
                    current_data = config_data
                    for part in key_parts[:-1]:
                        current_data = current_data.get(part, {})
                    if current_data.get(key_parts[-1]) != value:
                        continue

            # Check if required section exists
            if section.name not in config_data:
                if section.required:
                    raise ConfigValidationError(f"Required section '{section.name}' is missing")
                continue

            section_data = config_data[section.name]
            if not isinstance(section_data, dict):
                raise ConfigValidationError(f"Section '{section.name}' must be a dictionary")

            # Check for unknown fields in this section
            known_fields = self._get_known_fields(section)
            self._check_unknown_fields(section.name, section_data, known_fields)

            validated_section = {}
            # Validate each field in the section
            for field in section.fields:
                # Check if field exists in configuration
                if field.name in section_data:
                    value = section_data[field.name]
                    # Always validate if field is present, regardless of required status
                    try:
                        if field.is_list:
                            if not isinstance(value, list):
                                raise ConfigValidationError(
                                    f"Field '{field.name}' in section '{section.name}' must be a list"
                                )
                            # Validate each item in the list
                            value = [self._validate_field_value(item, field) for item in value]
                        else:
                            value = self._validate_field_value(value, field)
                    except ConfigValidationError as e:
                        raise ConfigValidationError(
                            f"Validation failed for field '{field.name}' in section '{section.name}': {str(e)}"
                        )
                else:
                    # Field is missing
                    if field.required:
                        raise ConfigValidationError(
                            f"Required field '{field.name}' is missing in section '{section.name}'"
                        )
                    value = field.default

                validated_section[field.name] = value

            validated_config[section.name] = validated_section

        return validated_config

    def _validate_field_value(self, value: Any, field: ConfigField) -> Any:
        """
        Validate a single field value against its schema definition.

        Args:
            value: The value to validate
            field: The field schema to validate against

        Returns:
            The validated (and possibly converted) value

        Raises:
            ConfigValidationError: If validation fails
        """
        # Validate type
        if isinstance(field.type, dict):
            if not isinstance(value, dict):
                raise ConfigValidationError("Value must be a dictionary")
            # Validate nested dictionary structure
            for key, expected_type in field.type.items():
                # expected_type is a ConfigField
                if key not in value:
                    if expected_type.required:
                        raise ConfigValidationError(f"Missing required key '{key}'")
                    else:
                        continue

                if expected_type.is_list:
                    if not isinstance(value[key], list):
                        raise ConfigValidationError(f"Field '{key}' in section '{field.name}' must be a list")
                    # Validate each item in the list
                    for item in value[key]:
                        self._validate_field_value(item, expected_type)
                else:
                    self._validate_field_value(value[key], expected_type)

        elif not isinstance(value, field.type):
            try:
                value = field.type(value)
            except (ValueError, TypeError):
                raise ConfigValidationError(f"Invalid type, expected {field.type.__name__}")

        # Validate choices
        if field.choices and value not in field.choices:
            raise ConfigValidationError(f"Value must be one of {field.choices}")

        # Run custom validator if provided
        if field.validator:
            if not field.validator.validate(value):
                raise ConfigValidationError(field.validator.get_error_message())

        return value
