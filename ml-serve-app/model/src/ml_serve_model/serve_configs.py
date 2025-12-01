from typing import Union, Optional, List

from tortoise import models, fields

from ml_serve_core.dtos.dtos import FastApiConfig, WorkerNodeConfig, HeadNodeConfig
from ml_serve_model.enums import BackendType


class APIServeInfraConfig(models.Model):
    id = fields.IntField(pk=True)

    serve = fields.ForeignKeyField(
        "models.Serve",
        related_name="api_serve_configs",
        on_delete=fields.CASCADE
    )

    environment = fields.ForeignKeyField(
        "models.Environment",
        on_delete=fields.CASCADE
    )

    backend_type = fields.CharField(max_length=50, choices=[bt.value for bt in BackendType])

    additional_hosts = fields.TextField(null=True)

    fast_api_config = fields.JSONField(null=True)

    created_by = fields.ForeignKeyField('models.User', related_name="created_by_api_serve", db_column='created_by')

    updated_by = fields.ForeignKeyField('models.User', related_name="updated_by_api_serve", db_column='updated_by')

    created_at = fields.DatetimeField(auto_now_add=True)

    updated_at = fields.DatetimeField(auto_now=True)

    class Meta:
        table = "api_serve_configs"

    # Internal helper to parse the JSON field

    @property
    def fast_api_config_object(self):
        return FastApiConfig(**self.fast_api_config)

    @property
    def additional_hosts_list(self):
        return self.additional_hosts.split(",") if self.additional_hosts else None


class WorkflowServeInfraConfig(models.Model):
    id = fields.IntField(pk=True)

    serve = fields.ForeignKeyField(
        "models.Serve",
        related_name="workflow_serve_configs",
        on_delete=fields.CASCADE
    )

    environment = fields.ForeignKeyField(
        "models.Environment",
        on_delete=fields.CASCADE
    )

    schedule = fields.CharField(max_length=50, null=False)

    worker_node_config = fields.JSONField(null=False)

    head_node_config = fields.JSONField(null=False)

    created_by = fields.ForeignKeyField('models.User', related_name="created_by_workflow_serve", db_column='created_by')

    updated_by = fields.ForeignKeyField('models.User', related_name="updated_by_workflow_serve", db_column='updated_by')

    created_at = fields.DatetimeField(auto_now_add=True)

    updated_at = fields.DatetimeField(auto_now=True)

    # Getter for worker_node_config
    @property
    def worker_node_config_list(self) -> List[WorkerNodeConfig]:
        if self.worker_node_config:
            return [WorkerNodeConfig(**item) for item in self.worker_node_config]
        return []

    # Setter for worker_node_config
    @worker_node_config_list.setter
    def worker_node_config_list(self, configs: List[WorkerNodeConfig]):
        if not isinstance(configs, list):
            raise ValueError("worker_node_config must be a list of WorkerNodeConfig objects.")
        self.worker_node_config = [config.dict() for config in configs]

    @property
    def head_node_config_object(self):
        return HeadNodeConfig(**self.head_node_config)

    @head_node_config_object.setter
    def head_node_config(self, config: HeadNodeConfig):
        self.head_node_config = config.dict()

    class Meta:
        table = "workflow_serve_configs"


ServeConfig = Union[Optional[APIServeInfraConfig], Optional[WorkflowServeInfraConfig]]
