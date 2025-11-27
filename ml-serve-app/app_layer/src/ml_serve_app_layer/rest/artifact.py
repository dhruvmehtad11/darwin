import traceback

from fastapi import APIRouter
from loguru import logger

from ml_serve_app_layer.dtos.requests import CreateArtifactRequest
from ml_serve_app_layer.utils.auth_utils import AuthorizedUser
from ml_serve_app_layer.utils.exception_util import handle_exception
from ml_serve_app_layer.utils.response_util import Response
from ml_serve_core.client.artifact_builder_client import ArtifactBuilderClient
from fastapi.responses import JSONResponse

from ml_serve_core.config.configs import Config
from ml_serve_core.service.artifact_service import ArtifactService
from ml_serve_core.service.serve_service import ServeService
from ml_serve_model import ArtifactBuilderJob
from ml_serve_model.enums import ServeType
from typing import Optional


class ArtifactRouter:
    def __init__(self):
        self.config = Config()
        self.router = APIRouter()
        self.artifact_service = ArtifactService()
        self.artifact_builder_client = ArtifactBuilderClient()
        self.serve_service = ServeService()
        self.register_routes()

    def register_routes(self):
        self.router.post("/artifact")(self.create_artifact)
        self.router.get("/artifact/{serve_name}")(self.get_artifacts)
        self.router.get("/artifact_builder_job/{job_id}/status")(self.get_status)
        self.router.get("/artifact_builder_job")(self.list_artifact_builder_jobs)

    async def create_artifact(
            self,
            request: CreateArtifactRequest,
            user: AuthorizedUser
    ) -> JSONResponse:
        try:
            serve = await self.serve_service.get_serve_by_name(request.serve_name)

            # check if serve is not present
            if not serve:
                return Response.bad_request_error_response(f"Serve not found for the given name {request.serve_name}")

            # check if version already exists
            if await self.artifact_service.check_version_already_exists(serve.id, request.version):
                return Response.bad_request_error_response(f"Version {request.version} already exists")

            if await self.artifact_service.check_artifact_builder_job_exists(serve.id, request.version):
                return Response.bad_request_error_response(
                    f"Artifact is in creating state for {request.version}")

            previous_artifact = await self.artifact_service.get_latest_artifact_by_serve_id(serve.id)

            if not previous_artifact:
                # check if giturl is not present
                if not request.github_repo_url:
                    return Response.bad_request_error_response("Git URL is required for the first artifact")

            # check if giturl is not present in the request then take the previous artifact giturl
            if not request.github_repo_url:
                request.github_repo_url = previous_artifact.github_repo_url

            # check if serve type is workflow then create workflow artifact else create artifact builder job
            if serve.type == ServeType.WORKFLOW.value:
                request.validation_for_workflow_serve_artifact_create_request()
                if not request.file_path:
                    request.file_path = previous_artifact.file_path
                resp = await self.artifact_service.create_workflow_serve_artifact(serve, request, user)
                return Response.created_response("Artifact successfully created", resp)

            resp = await self.artifact_builder_client.create_artifact(request, serve)

            artifact_builder_job = await self.artifact_service.create_artifact_builder_job(
                serve, request, resp["task_id"], user
            )

            return Response.created_response("Artifact creation job is submitted", {"job_id": artifact_builder_job.id})
        except Exception as e:
            tb = traceback.format_exc()
            logger.exception(f"Error while creating artifact: {e} - {tb}")
            return handle_exception(e)

    async def get_status(self, job_id) -> JSONResponse:
        try:
            if not await ArtifactBuilderJob.exists(id=job_id):
                return Response.bad_request_error_response(f"Job not found for the given id {job_id}")
            artifact_builder_job = await ArtifactBuilderJob.get(id=job_id)
            return Response.success_response(
                "Artifact status fetched successfully",
                {
                    "version": artifact_builder_job.version,
                    "status": artifact_builder_job.status,
                    "logs_url": f"{self.config.get_artifact_builder_public_url}/static/{artifact_builder_job.task_id}.log"
                }
            )
        except Exception as e:
            tb = traceback.format_exc()
            logger.exception(f"Error while fetching artifact status: {e} - {tb}")
            return handle_exception(e)

    async def get_artifacts(self, serve_name: str):
        try:
            serve = await self.serve_service.get_serve_by_name(serve_name)
            if not serve:
                return Response.bad_request_error_response(f"Serve not found for the given name {serve_name}")

            artifacts = await self.artifact_service.get_artifacts_by_serve_id(serve.id)
            return Response.success_response(
                "Artifacts fetched successfully",
                {"data": artifacts}
            )
        except Exception as e:
            tb = traceback.format_exc()
            logger.exception(f"Error while fetching artifacts: {e} - {tb}")
            return handle_exception(e)

    async def get_artifact(self, serve_name: str, version: str):
        try:
            serve = await self.serve_service.get_serve_by_name(serve_name)
            if not serve:
                return Response.bad_request_error_response(f"Serve not found for the given name {serve_name}")

            artifact = await self.artifact_service.get_artifact_by_version(serve.id, version)
            if not artifact:
                return Response.bad_request_error_response(f"Artifact not found for the given version {version}")

            return Response.success_response(
                "Artifact fetched successfully",
                {"data": artifact}
            )
        except Exception as e:
            tb = traceback.format_exc()
            logger.exception(f"Error while fetching artifact: {e} - {tb}")
            return handle_exception(e)

    async def list_artifact_builder_jobs(self, serve_name: Optional[str] = None, status: Optional[str] = None):
        try:
            jobs = await self.artifact_service.list_artifact_builder_jobs(
                serve_name=serve_name,
                status=status
            )
            return Response.success_response(
                "Artifact builder jobs fetched successfully",
                {"data": jobs}
            )
        except Exception as e:
            tb = traceback.format_exc()
            logger.exception(f"Error while listing artifact builder jobs: {e} - {tb}")
            return handle_exception(e)


artifact_router = ArtifactRouter().router
