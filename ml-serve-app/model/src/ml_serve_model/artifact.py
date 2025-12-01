from tortoise import fields, models


class Artifact(models.Model):
    id = fields.IntField(pk=True)
    serve = fields.ForeignKeyField('models.Serve', related_name='artifacts_serve', db_column='serve_id')
    version = fields.CharField(max_length=50)
    github_repo_url = fields.TextField(null=False)
    image_url = fields.TextField(null=True)
    branch = fields.CharField(max_length=50, null=True)
    file_path = fields.CharField(max_length=255, null=True)
    artifact_job = fields.ForeignKeyField(
        'models.ArtifactBuilderJob', related_name='artifacts_job', db_column='job_id', null=True
    )
    created_at = fields.DatetimeField(auto_now_add=True)
    created_by = fields.ForeignKeyField('models.User', related_name="created_by_artifact", db_column='created_by')

    class Meta:
        table = "artifacts"
        unique_together = ("serve", "version")
