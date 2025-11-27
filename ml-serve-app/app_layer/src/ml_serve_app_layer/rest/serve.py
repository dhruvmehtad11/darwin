from fastapi import APIRouter

from ml_serve_app_layer.dtos.requests import ServeConfigRequest, CreateServeRequest
from ml_serve_app_layer.utils.auth_utils import AuthorizedUser
from ml_serve_app_layer.utils.response_util import Response
from ml_serve_core.service.deployment_service import DeploymentService
from ml_serve_core.service.environment_service import EnvironmentService
from ml_serve_core.service.serve_config_service import ServeConfigService
from ml_serve_core.service.serve_service import ServeService
from fastapi.responses import JSONResponse

from ml_serve_model.active_deployment import ActiveDeployment
from ml_serve_model.enums import ServeType, DeploymentStatus
from ml_serve_model.serve_configs import APIServeInfraConfig, WorkflowServeInfraConfig


class ServeRouter:
    def __init__(self):
        self.router = APIRouter()
        self.serve_service = ServeService()
        self.serve_config_service = ServeConfigService()
        self.register_routes()
        self.deployment_service = DeploymentService()
        self.environment_service = EnvironmentService()

    def register_routes(self):
        self.router.get("")(self.get_serves)
        self.router.post("")(self.create_serve)
        self.router.get("/{serve_name}")(self.get_serve_overview)
        self.router.get("/{serve_name}/infra-config/{env}")(self.get_config)
        self.router.post("/{serve_name}/infra-config/{env}")(self.create_config)
        self.router.patch("/{serve_name}/infra-config/{env}")(self.update_config)
        self.router.get("/{serve_name}/status/{env}")(self.get_status)
        self.router.post("/{serve_name}/undeploy/{env}")(self.undeploy)

    async def get_serves(self, user: AuthorizedUser, ) -> JSONResponse:
        serves = await self.serve_service.get_serves()
        return Response.success_response("Serves retrieved successfully", serves)

    async def create_serve(
            self,
            request: CreateServeRequest,
            user: AuthorizedUser
    ) -> JSONResponse:
        existing_serve = await self.serve_service.get_serve_by_name(request.name)

        if existing_serve:
            return Response.conflict_error_response(f"Serve with name {request.name} already exists")

        serve = await self.serve_service.create_serve(request, user)

        return Response.created_response("Serve created successfully", serve)

    async def get_config(
            self,
            serve_name: str,
            env: str,
            user: AuthorizedUser
    ) -> JSONResponse:
        """
        Get the APIServeConfig for a given serve.
        """
        serve = await self.serve_service.get_serve_by_name(serve_name)

        environment = await self.environment_service.get_environment_by_name(env)

        if not serve:
            return Response.not_found_error_response(f"Serve with name {serve_name} not found")

        config = await self.serve_config_service.get_serve_config(
            serve.id, environment.id, serve.type
        )

        return Response.success_response("Serve configuration retrieved successfully", config)

    async def create_config(
            self,
            serve_name: str,
            env: str,
            request: ServeConfigRequest,
            user: AuthorizedUser
    ) -> JSONResponse:
        """
        Create a new APIServeConfig.
        """

        request.validation_for_create_request()

        serve = await self.serve_service.get_serve_by_name(serve_name)

        env = await self.environment_service.get_environment_by_name(env)

        if not env:
            return Response.not_found_error_response(f"Environment with name {env} not found")

        if not serve:
            return Response.not_found_error_response(f"Serve with name {serve_name} not found")

        if await self.serve_config_service.get_serve_config(serve.id, env.id, serve.type):
            return Response.conflict_error_response(f"Serve config already exists for serve with name {serve_name} for environment {env.name}")
        if await APIServeInfraConfig.filter(serve=serve, environment=env).exists() or \
                await WorkflowServeInfraConfig.filter(serve=serve, environment=env).exists():
            return Response.conflict_error_response(f"Serve config already exists for serve with name {serve_name} for environment {env.name}")

        if serve.type == ServeType.API.value and request.api_serve_config is None:
            return Response.bad_request_error_response(
                f"Serve with name {serve_name} is of API type but API config is empty")

        if serve.type == ServeType.WORKFLOW.value and request.workflow_serve_config is None:
            return Response.bad_request_error_response(
                f"Serve with name {serve_name} is of WORKFLOW type but Workflow config is empty"
            )

        config = None

        if request.api_serve_config:
            # Save to the database
            api_config_request = request.api_serve_config
            config = await APIServeInfraConfig.create(
                serve=serve,
                backend_type=api_config_request.backend_type.value,
                fast_api_config=api_config_request.fast_api_config.dict(),
                additional_hosts=','.join(
                    api_config_request.additional_hosts) if api_config_request.additional_hosts else None,
                created_by=user,
                updated_by=user,
                environment=env
            )

        if request.workflow_serve_config:
            # Save to the database
            workflow_config_request = request.workflow_serve_config
            config = await WorkflowServeInfraConfig.create(
                serve=serve,
                schedule=workflow_config_request.schedule,
                worker_node_config=workflow_config_request.worker_node_config,
                head_node_config=workflow_config_request.head_node_config,
                created_by=user,
                updated_by=user,
                environment=env
            )

        return Response.created_response("Serve configuration created successfully", config)

    async def get_serve_overview(self, serve_name: str, user: AuthorizedUser) -> JSONResponse:
        serve = await self.serve_service.get_serve_by_name(serve_name)
        if not serve:
            return Response.not_found_error_response(f"Serve with name {serve_name} not found")

        serve_info = {
            "id": serve.id,
            "name": serve.name,
            "type": serve.type,
            "description": serve.description,
            "space": serve.space,
            "created_at": serve.created_at,
            "updated_at": serve.updated_at,
        }

        infra_configs = []
        api_configs = await APIServeInfraConfig.filter(serve=serve)
        for cfg in api_configs:
            env = await cfg.environment
            infra_configs.append({
                "env": env.name if env else None,
                "type": "API",
                "backend_type": cfg.backend_type,
                "fast_api_config": cfg.fast_api_config,
                "additional_hosts": cfg.additional_hosts.split(',') if cfg.additional_hosts else None,
                "created_at": cfg.created_at,
                "updated_at": cfg.updated_at,
            })

        workflow_configs = await WorkflowServeInfraConfig.filter(serve=serve)
        for cfg in workflow_configs:
            env = await cfg.environment
            infra_configs.append({
                "env": env.name if env else None,
                "type": "WORKFLOW",
                "schedule": cfg.schedule,
                "head_node_config": cfg.head_node_config,
                "worker_node_config": cfg.worker_node_config,
                "created_at": cfg.created_at,
                "updated_at": cfg.updated_at,
            })

        return Response.success_response("Serve overview retrieved successfully", {
            "serve": serve_info,
            "infra_configs": infra_configs
        })

    async def update_config(
            self,
            serve_name: str,
            env: str,
            request: ServeConfigRequest,
            user: AuthorizedUser
    ) -> JSONResponse:
        """
        Update an existing APIServeConfig.
        """
        serve = await self.serve_service.get_serve_by_name(serve_name)

        if not serve:
            return Response.not_found_error_response(f"Serve with name {serve_name} not found")

        environment = await self.environment_service.get_environment_by_name(env)

        if not environment:
            return Response.not_found_error_response(f"Environment with name {env} not found")

        if serve.type == ServeType.API.value and request.api_serve_config is None:
            return Response.bad_request_error_response(
                f"Serve with name {serve_name} is of API type but API config is empty")

        if serve.type == ServeType.WORKFLOW.value and request.workflow_serve_config is None:
            return Response.bad_request_error_response(
                f"Serve with name {serve_name} is of WORKFLOW type but Workflow config is empty"
            )

        if serve.type == ServeType.API.value:
            config = await self.serve_config_service.update_api_serve_configs(
                serve, environment, request.api_serve_config
            )
            await self.deployment_service.redeploy_api_serve_with_updated_infra_config(
                serve=serve, api_serve_config=config, env=environment, user=user
            )
        else:
            config = await self.serve_config_service.update_workflow_serve_configs(
                serve, environment, request.workflow_serve_config
            )
            await self.deployment_service.redeploy_workflow_serve_with_updated_infra_config(
                serve=serve, workflow_serve_config=config, env=environment, user=user
            )

        return Response.success_response("Serve configuration updated successfully", config)

    async def get_status(
            self,
            serve_name: str,
            env: str,
            user: AuthorizedUser
    ) -> JSONResponse:
        """
        Get the status of the serve.
        """
        serve = await self.serve_service.get_serve_by_name(serve_name)

        environment = await self.environment_service.get_environment_by_name(env)

        if not serve:
            return Response.not_found_error_response(f"Serve with name {serve_name} not found")

        if not environment:
            return Response.bad_request_error_response(f"Environment with name {env} not found")

        status = await self.serve_service.get_serve_status(serve, environment)

        return Response.success_response("Serve status retrieved successfully", {"status": status})

    async def undeploy(
            self,
            serve_name: str,
            env: str,
            user: AuthorizedUser
    ) -> JSONResponse:
        serve = await self.serve_service.get_serve_by_name(serve_name)
        environment = await self.environment_service.get_environment_by_name(env)

        if not serve:
            return Response.not_found_error_response(f"Serve with name {serve_name} not found")
        if not environment:
            return Response.bad_request_error_response(f"Environment with name {env} not found")

        # For API serves, stop the k8s resource via DCM
        if serve.type == ServeType.API.value:
            resource_id = f"{environment.name}-{serve.name}"
            # Always verify with DCM status first to determine if resource is present/running
            try:
                _status = await self.deployment_service.dcm_client.get_status(
                    resource_id=resource_id,
                    kube_cluster=environment.cluster_name,
                    kube_namespace=environment.namespace,
                )
            except Exception:
                return Response.success_response("No running resource found; nothing to undeploy")

            # DCM confirms resource presence → proceed to stop
            await self.deployment_service.dcm_client.stop_resource(
                resource_id=resource_id,
                kube_cluster=environment.cluster_name,
                namespace=environment.namespace,
            )

            # If we had an active pointer, mark it ENDED and clear pointer
            active = await ActiveDeployment.get_or_none(serve=serve, environment=environment)
            if active:
                current_deployment = await active.deployment
                current_deployment.status = DeploymentStatus.ENDED.value
                from datetime import datetime, timezone
                current_deployment.ended_at = datetime.now(timezone.utc)
                await current_deployment.save()
                await active.delete()

            return Response.success_response("Serve undeploy initiated successfully")

        # For workflow serves, this could disable schedule/stop workers (not implemented yet)
        return Response.bad_request_error_response("Undeploy not supported for workflow serves yet")


serve_router = ServeRouter().router
