"""
Utility functions for S3 bucket operations.
"""
import os
from typing import Optional
from urllib.parse import urlparse

import boto3
from botocore.exceptions import ClientError
from typeguard import typechecked

from mlflow_app_layer.util.logging_util import get_logger

logger = get_logger(__name__)


@typechecked
def get_s3_client():
    """
    Create and return an S3 client configured with LocalStack or AWS credentials.
    Uses AWS_ENDPOINT_OVERRIDE for the endpoint URL.
    
    Returns:
        boto3.client: Configured S3 client
    """
    # Use AWS_ENDPOINT_OVERRIDE as primary, fallback to MLFLOW_S3_ENDPOINT_URL
    endpoint_url = os.getenv("AWS_ENDPOINT_OVERRIDE") or os.getenv("MLFLOW_S3_ENDPOINT_URL")
    aws_access_key_id = os.getenv("AWS_ACCESS_KEY_ID", "test")
    aws_secret_access_key = os.getenv("AWS_SECRET_ACCESS_KEY", "test")
    region_name = os.getenv("AWS_DEFAULT_REGION", "us-east-1")

    s3_client = boto3.client(
        "s3",
        endpoint_url=endpoint_url,
        aws_access_key_id=aws_access_key_id,
        aws_secret_access_key=aws_secret_access_key,
        region_name=region_name,
    )
    return s3_client


@typechecked
def extract_bucket_name(s3_path: str) -> Optional[str]:
    """
    Extract bucket name from S3 path.
    
    Args:
        s3_path: S3 path in format s3://bucket-name/path
        
    Returns:
        str: Bucket name or None if path is invalid
    """
    if not s3_path or not s3_path.startswith("s3://"):
        return None
    
    parsed = urlparse(s3_path)
    return parsed.netloc


@typechecked
def ensure_s3_bucket_exists(s3_path: str) -> bool:
    """
    Check if S3 bucket exists, create it if it doesn't.
    
    Args:
        s3_path: S3 path in format s3://bucket-name/path
        
    Returns:
        bool: True if bucket exists or was created successfully, False otherwise
    """
    bucket_name = extract_bucket_name(s3_path)
    if not bucket_name:
        logger.error(f"Invalid S3 path format: {s3_path}")
        return False

    try:
        s3_client = get_s3_client()
        
        # Check if bucket exists
        try:
            s3_client.head_bucket(Bucket=bucket_name)
            logger.info(f"S3 bucket '{bucket_name}' already exists")
            return True
        except ClientError as e:
            error_code = e.response.get("Error", {}).get("Code", "")
            if error_code == "404":
                # Bucket doesn't exist, create it
                logger.info(f"S3 bucket '{bucket_name}' does not exist, creating...")
                try:
                    s3_client.create_bucket(Bucket=bucket_name)
                    logger.info(f"S3 bucket '{bucket_name}' created successfully")
                    return True
                except ClientError as create_error:
                    logger.error(
                        f"Failed to create S3 bucket '{bucket_name}': {create_error}"
                    )
                    return False
            else:
                # Other error (e.g., access denied, connection error)
                logger.error(
                    f"Error checking S3 bucket '{bucket_name}': {e.response.get('Error', {}).get('Message', str(e))}"
                )
                return False
                
    except Exception as e:
        logger.error(f"Unexpected error while ensuring S3 bucket exists: {e}", exc_info=True)
        return False


@typechecked
def initialize_s3_bucket() -> bool:
    """
    Initialize S3 bucket from environment variable MLFLOW_S3_BUCKET.
    Uses AWS_ENDPOINT_OVERRIDE or MLFLOW_S3_ENDPOINT_URL for the S3 endpoint.
    Called during app startup.
    
    Returns:
        bool: True if bucket exists or was created successfully, False otherwise
    """
    bucket_name = os.getenv("MLFLOW_S3_BUCKET")
    if not bucket_name:
        logger.warning("MLFLOW_S3_BUCKET environment variable is not set, skipping S3 bucket initialization")
        return False
    
    endpoint_url = os.getenv("AWS_ENDPOINT_OVERRIDE") or os.getenv("MLFLOW_S3_ENDPOINT_URL")
    if endpoint_url:
        logger.info(f"Using S3 endpoint: {endpoint_url}")
    
    s3_path = f"s3://{bucket_name}"
    logger.info(f"Initializing S3 bucket: {s3_path}")
    return ensure_s3_bucket_exists(s3_path)

