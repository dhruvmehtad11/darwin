from enum import Enum

from pydantic import BaseModel


class Cloud(Enum):
    AWS = "aws"
    GCP = "gcp"


class EC2SpotMetric(BaseModel):
    ec2_type: str
    cloud: Cloud
    region: str
    account_name: str


class TimeRangeRequest(BaseModel):
    start_time: str
    end_time: str
