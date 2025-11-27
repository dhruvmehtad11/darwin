import boto3

from compute_core.constant.config import Config


def download_file_content_from_s3(path: str) -> str:
    """
    Downloads the file content from the given S3 path
    :param path: S3 path of the file
    :return: file content
    """
    s3 = boto3.client("s3")
    bucket = path.split("/")[2]
    key = "/".join(path.split("/")[3:])
    obj = s3.get_object(Bucket=bucket, Key=key)
    return obj["Body"].read().decode("utf-8")


def download_file_content_from_workspace(path: str) -> str:
    """
    Downloads the file content from the given S3 path for workspace package
    :param path: S3 path of the file
    :return: file content
    """
    bucket_path = Config().get_workspace_packages_bucket
    return download_file_content_from_s3(f"${bucket_path}/{path}")
