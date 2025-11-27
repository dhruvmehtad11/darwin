import json
from datetime import datetime
from enum import Enum

from pydantic import BaseModel, Field
from shortuuid import ShortUUID

from compute_core.constant.config import Config


class SparkHSFilesystem(Enum):
    S3 = "s3"
    EFS = "efs"


class SparkHSStatus(Enum):
    CREATED = "created"
    ACTIVE = "active"
    INACTIVE = "inactive"
    FAILED = "failed"


class SparkHistoryServer(BaseModel):
    resource: str
    user: str
    ttl: int = Field(default=60)  # TTL is in minutes
    filesystem: SparkHSFilesystem = Field(default=SparkHSFilesystem.S3)
    events_path: str = Field(default=None)
    cloud_env: str = Field(default=None)
    started_at: datetime = Field(init_var=False, default_factory=lambda: str(datetime.utcnow()))
    status: SparkHSStatus = Field(default=SparkHSStatus.CREATED)
    id: str = Field(
        init_var=False,
        default_factory=lambda: f"shs-{ShortUUID(alphabet='abcdefghijklmnopqrstuvwxyz0123456789').random()}",
    )
    url: str = Field(default=None, init_var=False)

    def __init__(self, **data):
        super().__init__(**data)
        if self.events_path is None:
            if self.filesystem == SparkHSFilesystem.S3:
                self.events_path = f"{Config().spark_history_server_config['s3_events_path']}/{self.resource}"
            elif self.filesystem == SparkHSFilesystem.EFS:
                self.events_path = f"{Config().spark_history_server_config['efs_events_path']}/{self.resource}"
        else:
            if self.filesystem == SparkHSFilesystem.S3:
                if self.events_path.startswith("s3"):
                    self.events_path = self.events_path.replace("s3://", "s3a://", 1)
                else:
                    self.events_path = f"s3a://{self.events_path.lstrip('/')}"

    def model_dump(self) -> dict:
        return json.loads(self.json())
