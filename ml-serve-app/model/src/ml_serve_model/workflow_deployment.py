from tortoise import fields, models


class ScheduledWorkflowDeployment(models.Model):
    """
    Child table for Scheduled Workflow Deployments, referencing the parent Deployments row.
    """
    id = fields.IntField(pk=True)

    # Link to the parent deployment row
    deployment = fields.OneToOneField(
        "models.Deployment",
        related_name="scheduled_workflow_deployments",
        on_delete=fields.CASCADE
    )

    workflow_id = fields.CharField(max_length=255)

    job_cluster_id = fields.CharField(max_length=255)

    input_params = fields.JSONField(null=True)

    created_at = fields.DatetimeField(auto_now_add=True)

    class Meta:
        table = "scheduled_workflow_deployments"
