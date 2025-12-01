from fastapi import APIRouter

from ml_serve_app_layer.dtos.requests import EnvironmentRequest, EnvironmentConfigRequest
from ml_serve_app_layer.utils.auth_utils import AuthorizedUser
from ml_serve_app_layer.utils.response_util import Response
from ml_serve_core.service.environment_service import EnvironmentService
from ml_serve_model import Environment, Deployment, ActiveDeployment, APIServeInfraConfig, WorkflowServeInfraConfig


class EnvironmentRouter:
    def __init__(self):
        self.environment_service = EnvironmentService()
        self.router = APIRouter()
        self.register_routes()

    def register_routes(self):
        self.router.get("/environment/{env_name}")(self.get_environment)
        self.router.patch("/environment/{env_name}")(self.update_environment)
        self.router.delete("/environment/{env_name}")(self.delete_environment)
        self.router.post("/environment")(self.create_environment)

    async def get_environment(self, env_name: str):
        return await self.environment_service.get_environment_by_name(env_name)

    async def create_environment(self, request: EnvironmentRequest, user: AuthorizedUser):
        request.validation_for_create_request()

        # Check if environment with this name already exists
        existing_env = await self.environment_service.get_environment_by_name(request.name)
        if existing_env:
            return Response.conflict_error_response(
                f"Environment with name '{request.name}' already exists. Environment names must be unique."
            )

        env = await Environment.create(
            name=request.name,
            is_protected=False,  # Default to false, can be changed in database if needed
            env_configs=request.environment_configs.dict()
        )
        return Response.success_response(
            f"Environment '{request.name}' created successfully",
            {"id": env.id, "name": env.name}
        )

    async def update_environment(self, env_name: str, request: EnvironmentConfigRequest, user: AuthorizedUser):
        env = await Environment.get(name=env_name)

        if not env:
            return Response.not_found_error_response(f"Environment with name {env_name} not found")

        if request.cluster_name:
            env.cluster_name = request.cluster_name

        if request.domain_suffix:
            env.domain_suffix = request.domain_suffix

        if request.security_group:
            env.security_group = request.security_group

        if request.subnets:
            env.subnets = request.subnets

        if request.ft_redis_url:
            env.ft_redis_url = request.ft_redis_url

        if request.workflow_url:
            env.workflow_url = request.workflow_url

        if request.namespace:
            env.namespace = request.namespace

        await env.save()

        return Response.success_response("Environment updated successfully")

    async def delete_environment(self, env_name: str, user: AuthorizedUser):
        env = await Environment.get(name=env_name)
        if not env:
            return Response.not_found_error_response(f"Environment with name {env_name} not found")
        # Safety: block deletion if referenced by other resources
        has_deployments = await Deployment.filter(environment=env).exists()
        has_active = await ActiveDeployment.filter(environment=env).exists()
        has_api_cfgs = await APIServeInfraConfig.filter(environment=env).exists()
        has_wkf_cfgs = await WorkflowServeInfraConfig.filter(environment=env).exists()

        if has_deployments or has_active or has_api_cfgs or has_wkf_cfgs:
            return Response.conflict_error_response(
                "Environment is referenced by existing resources (deployments/active/configs). Delete them first."
            )
        await env.delete()
        return Response.success_response("Environment deleted successfully")


environment_router = EnvironmentRouter().router
