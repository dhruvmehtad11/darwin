from typing import Union, Optional

from fastapi import HTTPException

from ml_serve_app_layer.dtos.requests import APIServeConfigRequest, WorkflowServeConfigCreateRequest
from ml_serve_core.service.environment_service import EnvironmentService
from ml_serve_model import APIServeInfraConfig, Environment, User
from ml_serve_model.serve import Serve
from ml_serve_model.enums import ServeType
from ml_serve_model.serve_configs import WorkflowServeInfraConfig, ServeConfig
from loguru import logger


class ServeConfigService:

    def __init__(self):
        self.environment_service = EnvironmentService()

    async def get_serve_config(
            self, serve_id: int, env_id: int, serve_type: str
    ) -> ServeConfig:
        if serve_type == ServeType.API.value:
            return await self.get_api_serve_config(serve_id, env_id)
        elif serve_type == ServeType.WORKFLOW.value:
            return await self.get_workflow_serve_config(serve_id, env_id)

    async def get_api_serve_config(self, serve_id: int, env_id: int) -> Optional[APIServeInfraConfig]:
        print(await APIServeInfraConfig.get_or_none(serve_id=serve_id, environment_id=env_id))
        return await APIServeInfraConfig.get_or_none(serve_id=serve_id, environment_id=env_id)

    async def get_workflow_serve_config(self, serve_id: int, env_id: int) -> Optional[WorkflowServeInfraConfig]:
        return await WorkflowServeInfraConfig.get_or_none(serve_id=serve_id, environment_id=env_id)

    async def update_api_serve_configs(
            self,
            serve: Serve,
            env: Environment,
            request: APIServeConfigRequest,
    ):
        """
        Update the API Serve Config.
        """
        config = await APIServeInfraConfig.get_or_none(serve=serve, environment=env)

        if not config:
            raise HTTPException(status_code=404, detail="Serve config not found")

        current_fast_api_config = config.fast_api_config_object

        if request.fast_api_config.cores:
            current_fast_api_config.cores = request.fast_api_config.cores

        if request.fast_api_config.memory:
            current_fast_api_config.memory = request.fast_api_config.memory

        if request.fast_api_config.node_capacity_type:
            current_fast_api_config.node_capacity_type = request.fast_api_config.node_capacity_type

        if request.fast_api_config.min_replicas:
            current_fast_api_config.min_replicas = request.fast_api_config.min_replicas

        if request.fast_api_config.max_replicas:
            current_fast_api_config.max_replicas = request.fast_api_config.max_replicas

        config.fast_api_config = current_fast_api_config.dict()

        await config.save()
        return config

    async def update_workflow_serve_configs(
            self,
            serve: Serve,
            env: Environment,
            request: WorkflowServeConfigCreateRequest,
    ):
        """
        Update the Workflow Serve Config.
        """
        config = await WorkflowServeInfraConfig.get_or_none(serve=serve, environment=env)

        if not config:
            raise HTTPException(status_code=404, detail="Serve config not found")

        if request.worker_node_config:
            config.worker_node_config = request.worker_node_config

        if request.head_node_config:
            config.head_node_config = request.head_node_config

        if request.schedule:
            config.schedule = request.schedule

        await config.save()
        return config
