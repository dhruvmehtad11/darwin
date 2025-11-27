import boto3
import os
import re
from datetime import datetime


def download_dockerfile_from_s3(s3_uri, local_path):
    global bucket_name, s3_key
    
    # Configure boto3 client with environment variables
    # For local development, AWS credentials can be set via environment variables
    aws_access_key_id = os.getenv("AWS_ACCESS_KEY_ID")
    aws_secret_access_key = os.getenv("AWS_SECRET_ACCESS_KEY")
    aws_region = os.getenv("AWS_DEFAULT_REGION", "us-east-1")
    
    # Create S3 client with explicit credentials if available
    if aws_access_key_id and aws_secret_access_key:
        s3 = boto3.client(
            "s3",
            aws_access_key_id=aws_access_key_id,
            aws_secret_access_key=aws_secret_access_key,
            region_name=aws_region
        )
    else:
        # Fallback to default credential chain (IAM roles, AWS config, etc.)
        s3 = boto3.client("s3")

    if s3_uri.startswith("s3://"):
        s3_path = s3_uri[5:]
        bucket_end_index = s3_path.index("/")
        bucket_name = s3_path[:bucket_end_index]
        s3_key = s3_path[bucket_end_index + 1 :]
    try:
        print(bucket_name, s3_key, local_path)
        s3.download_file(bucket_name, s3_key, local_path)
        return True
    except Exception as e:
        print(f"S3 download error: {e}")
        return False


def convert_github_url(url):
    github_token = os.getenv("GITHUB_TOKEN", "")
    
    match = re.search(r"https://github.com/(.*)", url)
    if not match:
        raise ValueError("Invalid GitHub URL")

    repository_path = match.group(1)
    
    if github_token:
        converted_url = f"https://{github_token}@github.com/{repository_path}"
    else:
        converted_url = url
    
    return converted_url


def parse_date(date: datetime) -> str:
    """
    :param date: datetime
    :return: Date in ISO 8601 format
    """
    date = str(date)
    return date.replace(" ", "T") + "Z"


