from enum import Enum

from tortoise import models, fields


class JobStatus(Enum):
    PENDING = "PENDING"
    SUCCESSFUL = "SUCCESSFUL"
    FAILED = "FAILED"


class ArtifactBuilderJob(models.Model):
    id = fields.IntField(pk=True)
    serve = fields.ForeignKeyField('models.Serve', related_name='artifacts_job_serve', db_column='serve_id')
    version = fields.CharField(max_length=50)
    github_repo_url = fields.TextField(null=False)
    branch = fields.CharField(max_length=255, null=True)
    task_id = fields.TextField(null=True)
    status = fields.CharField(max_length=50, default='PENDING')
    created_at = fields.DatetimeField(auto_now_add=True)
    last_picked_at = fields.DatetimeField()
    status_last_updated_at = fields.DatetimeField(null=True)
    created_by = fields.ForeignKeyField('models.User', related_name="created_by", db_column='created_by')

    class Meta:
        table = "artifact_builder_job"
