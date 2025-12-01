import io
import os
import boto3
import subprocess
from fastapi import File
from pathlib import Path
from serve_core.constant.serve_constants import Config


def upload_file_to_s3(bucket_name, s3_key, local_path):
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
    
    try:
        s3.upload_file(local_path, bucket_name, s3_key)
        return True
    except Exception as e:
        # Log the error for debugging in local development
        print(f"S3 upload error: {str(e)}")
        return False


async def upload_dockerfile(image_tag: str, app_name: str, env: str, file: bytes = File()):
    local_file_path = f"{os.getcwd()}/{image_tag}/Dockerfile"
    file_path = Path(local_file_path).expanduser()
    dir_path = file_path.parent
    if not dir_path.is_dir():
        dir_path.mkdir()
    with open(local_file_path, "wb") as f:
        f.write(file)

    _config = Config(env)
    dockerfile_s3_bucket = _config.get_s3_bucket
    dockerfile_s3_key = f"serve/dockerfiles/{app_name}/{image_tag}/Dockerfile"

    upload_successful = upload_file_to_s3(dockerfile_s3_bucket, dockerfile_s3_key, local_file_path)

    if not upload_successful:
        return {"message": "Upload to S3 failed"}

    return "s3://" + dockerfile_s3_bucket + "/" + dockerfile_s3_key


def is_docker_running():
    try:
        # Try without sudo first
        result = subprocess.run(["docker", "ps"], 
                              check=False, 
                              capture_output=True, 
                              text=True)
        if result.returncode == 0:
            return True
        
        # If that failed and we're not in dev mode, try with sudo
        if not os.getenv("DEV"):
            result = subprocess.run(["sudo", "docker", "ps"], 
                                  check=True, 
                                  capture_output=True)
            return True
        
        return False
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False
