from tortoise import models, fields


class ServiceAudit(models.Model):
    """
    Service audit table: tracks lifecycle events or state changes for a service.
    """
    id = fields.IntField(pk=True)

    service = fields.ForeignKeyField(
        "models.Service",
        related_name="audit_events",
        on_delete=fields.CASCADE
    )

    event_type = fields.CharField(max_length=50)  # e.g., "CREATED", "STATE_CHANGE"
    old_state = fields.CharField(max_length=50, null=True)
    new_state = fields.CharField(max_length=50, null=True)

    message = fields.TextField(null=True)
    occurred_at = fields.DatetimeField(auto_now_add=True)
    request_by = fields.ForeignKeyField(
        "models.Users",
        related_name="service_audit",
        on_delete=fields.SET_NULL,
        null=True
    )

    class Meta:
        table = "service_audit"

    def __str__(self):
        return (
            f"ServiceAudit(id={self.id}, service_id={self.service_id}, "
            f"event_type={self.event_type}, occurred_at={self.occurred_at})"
        )