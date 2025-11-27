import os
import json

from workspace_app_layer.exceptions.exceptions import BadRequestException
from workspace_core.constants.constants import BASE_EFS_PATH
from workspace_core.utils.logging_util import get_logger
from workspace_core.utils.s3_utils import is_file_size_valid, is_file_type_valid

logger = get_logger(__name__)


def get_file_contents(file_path: str):
    """
    Reads and parses a JSON file from EFS after validating size and type.
    """
    file_path = f"{BASE_EFS_PATH}/{file_path}"
    is_file_type_valid(file_path, (".json",))
    is_file_size_valid(file_path, 2)

    try:
        logger.info(f"Attempting to read file '{file_path}'")
        with open(file_path, "r") as f:
            data = json.load(f)
        logger.info(f"Successfully read JSON file: '{file_path}'")
        return data

    except FileNotFoundError:
        raise BadRequestException(f"File not found: {file_path}")
    except json.JSONDecodeError as e:
        raise BadRequestException(f"Invalid JSON content in file '{file_path}': {e}")
    except OSError as e:
        raise BadRequestException(f"Error reading file '{file_path}': {e}")
