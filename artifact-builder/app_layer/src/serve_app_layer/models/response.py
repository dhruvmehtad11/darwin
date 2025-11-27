from enum import Enum
from pydantic import BaseModel
from typing import List, Optional


class StatusEnum(str, Enum):
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"


class TaskEntity(BaseModel):
    task_id: str
    app_name: str
    image_tag: str
    logs_url: str
    status: str


class GetImageResponse(BaseModel):
    status: StatusEnum
    task_id: str
    logs_url: str
    message: Optional[str]


class GetAllTasksResponse(BaseModel):
    status: StatusEnum
    data: List[TaskEntity]
    message: Optional[str]


class GetBuildStatusResponse(BaseModel):
    status: StatusEnum
    build_status: str
    message: Optional[str]


class GetTaskLogsResponse(BaseModel):
    status: StatusEnum
    logs_url: str
    message: Optional[str]
