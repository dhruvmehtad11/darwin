from typing import Optional

from fastapi import HTTPException
from loguru import logger

from ml_serve_app_layer.dtos.requests import CreateServeRequest
from ml_serve_core.client.dcm_client import DCMClient
from ml_serve_core.client.http_client import AsyncHttpClient
from ml_serve_core.dtos.dtos import EnvConfig
from ml_serve_core.utils.utils import get_host_name
from ml_serve_model import Serve, User, Environment
from ml_serve_model.enums import ServeType


class ServeService:

    def __init__(self):
        self.dcm_client = DCMClient()

    async def get_serves(self):
        # Return plain dictionaries to avoid serialization issues with ORM models
        return await Serve.all().values()

    async def create_serve(self, request: CreateServeRequest, user: User) -> Serve:
        return await Serve.create(
            name=request.name,
            type=request.type.value,
            description=request.description,
            created_by=user,
            space=request.space
        )

    async def get_serve(self, serve_id: int) -> Serve:
        if not await Serve.exists(id=serve_id):
            raise HTTPException(status_code=404, detail=f"Serve not found for the given id {serve_id}")

        return await Serve.get(id=serve_id)

    async def get_serve_by_name(self, serve_name: str) -> Optional[Serve]:
        if not await Serve.exists(name=serve_name):
            return None

        return await Serve.get(name=serve_name)

    async def get_serve_status(self, serve: Serve, env: Environment):
        if serve.type == ServeType.API.value:
            return await self.get_api_serve_status(serve, env)
        elif serve.type == ServeType.WORKFLOW.value:
            return await self.get_workflow_serve_status(serve, env)

    async def get_api_serve_status(self, serve, env):
        dcm_status = await self.dcm_client.get_status(
            resource_id=f"{env.name}-{serve.name}",
            kube_cluster=env.cluster_name,
            kube_namespace=env.namespace,
        )
        pods_status = dcm_status.get("pods", [])
        host_name = get_host_name(serve.name, env.name, EnvConfig(**env.env_configs), env.is_protected)

        healthcheck_status = True
        try:
            async with AsyncHttpClient() as client:
                await client.get(f"http://{host_name}/healthcheck")
        except Exception as e:
            logger.info(f"Healthcheck failed for {host_name}: {e.__str__()}")
            healthcheck_status = False

        status = "READY" if all(
            pod["status"] == "Running" for pod in pods_status) and healthcheck_status else "NOT_READY"
        return status

    def get_workflow_serve_status(self, serve, env):
        # TODO: Implement this method
        pass
