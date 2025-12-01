from enum import Enum
from tortoise import models, fields


class BuildStatus(str, Enum):
    WAITING = "waiting"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"


class ImageBuilder(models.Model):
    """
    Model for tracking Docker image build tasks
    """
    task_id = fields.CharField(max_length=255, pk=True)
    app_name = fields.CharField(max_length=255)
    image_tag = fields.CharField(max_length=255)
    logs_url = fields.TextField(null=True)
    build_params = fields.JSONField(null=True)
    status = fields.CharEnumField(BuildStatus, default=BuildStatus.WAITING)
    created_at = fields.DatetimeField(auto_now_add=True)
    updated_at = fields.DatetimeField(auto_now=True)

    class Meta:
        table = "darwin_image_builder"
        indexes = [
            ("status",),
            ("created_at",),
        ]

    def __str__(self):
        return f"ImageBuilder({self.task_id}, {self.app_name}, {self.status})"

