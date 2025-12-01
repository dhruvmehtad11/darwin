from tortoise import models, fields
from datetime import datetime
from ml_serve_model.enums import DeploymentStatus


class Deployment(models.Model):
    """
    Deployment model to store deployment
    """
    id = fields.IntField(pk=True)

    serve = fields.ForeignKeyField(
        "models.Serve",
        related_name="deployments",
        on_delete=fields.CASCADE
    )

    artifact = fields.ForeignKeyField(
        "models.Artifact",
        related_name="deployments",
        on_delete=fields.CASCADE
    )

    environment = fields.ForeignKeyField(
        "models.Environment",
        related_name="deployments_environment",
        on_delete=fields.CASCADE
    )

    created_by = fields.ForeignKeyField(
        "models.User",
        related_name="deployments",
        on_delete=fields.CASCADE
    )

    created_at = fields.DatetimeField(auto_now_add=True)
    status = fields.CharField(max_length=20, default=DeploymentStatus.ACTIVE.value)
    ended_at = fields.DatetimeField(null=True)

    class Meta:
        table = "deployments"
