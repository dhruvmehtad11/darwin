import os
import boto3

from workspace_app_layer.exceptions.exceptions import BadRequestException
from workspace_core.constants.constants import (
    VALID_FILE_TYPES_FOR_UPLOAD,
    BASE_EFS_PATH,
    VALID_FILE_SIZE_IN_MB_FOR_UPLOAD,
)
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


def is_file_size_valid(file_path: str, max_size: int) -> bool:
    """
    Checks if given file size is less than max_size MB
    """
    if os.path.getsize(file_path) < max_size * 1024 * 1024:
        return True
    else:
        raise BadRequestException(f"File size is greater than {max_size} MB")


def is_file_type_valid(file_path: str, supported_file_types: tuple) -> bool:
    """
    Checks if given file type is valid
    """
    if file_path.endswith(supported_file_types):
        return True
    else:
        raise BadRequestException(f"File type not supported")


class S3Utils:
    def __init__(self):
        self.s3 = boto3.client(
            "s3",
            aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
            aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY"),
            region_name=os.getenv("AWS_DEFAULT_REGION"),
        )

    def upload_file(self, file_path: str, bucket_name: str, key: str):
        file_path = f"{BASE_EFS_PATH}/{file_path}"
        if is_file_size_valid(file_path, VALID_FILE_SIZE_IN_MB_FOR_UPLOAD) and is_file_type_valid(
            file_path, tuple(VALID_FILE_TYPES_FOR_UPLOAD)
        ):
            logger.info(f"Uploading file {file_path} to S3 bucket {bucket_name} with key {key}")
            return self.s3.upload_file(file_path, bucket_name, key)
