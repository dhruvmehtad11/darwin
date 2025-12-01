from loguru import logger
from typing import List, Dict, Any, Union
import os

import boto3



class EC2Client:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._client = boto3.client(
                "ec2",
                aws_access_key_id=os.environ.get("VAULT_SERVICE_ACCESS_KEY_ID"),
                aws_secret_access_key=os.environ.get("VAULT_SERVICE_SECRET_ACCESS_KEY"),
                region_name=os.environ.get("VAULT_SERVICE_DEFAULT_REGION", "us-east-1")
            )
        return cls._instance

    def describe_instances(self, filters: List[Dict[str, Union[str, List]]]) -> List[Dict[str, Any]]:
        try:
            response = self._client.describe_instances(Filters=filters)
            return response["Reservations"]
        except Exception as e:
            logger.exception("Error while describing instances", e)
            return []
