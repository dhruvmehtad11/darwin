from datetime import datetime, timezone
from typing import List, Optional

from tortoise.transactions import in_transaction

from ml_serve_app_layer.dtos.requests import CreateArtifactRequest
from ml_serve_core.constants.constants import CONTAINER_REGISTRY, IMAGE_REPOSITORY
from ml_serve_core.sql_queries import GET_UNPICKED_ARTIFACTS_QUERY
from ml_serve_model import Artifact, Serve, User
from ml_serve_model.artifact_builder_job import ArtifactBuilderJob
from ml_serve_model.enums import JobStatus


class ArtifactService:
    async def create_artifact_builder_job(
            self,
            serve: Serve,
            request: CreateArtifactRequest,
            task_id: str,
            created_by: User
    ) -> ArtifactBuilderJob:
        return await ArtifactBuilderJob.create(
            serve=serve,
            version=request.version,
            github_repo_url=request.github_repo_url,
            branch=request.branch if request.branch else "main",
            created_by=created_by,
            status_last_updated_at=datetime.now(timezone.utc),
            last_picked_at=datetime.now(timezone.utc),
            task_id=task_id,
            status=JobStatus.PENDING.value,
        )

    async def create_workflow_serve_artifact(
            self, serve: Serve, request: CreateArtifactRequest, created_by: User
    ) -> Artifact:
        return await Artifact.create(
            serve=serve,
            version=request.version,
            github_repo_url=request.github_repo_url,
            created_by=created_by,
            branch=request.branch,
            file_path=request.file_path,
        )

    async def get_artifacts_by_serve_id(self, serve_id: int) -> List[Artifact]:
        return await Artifact.filter(serve_id=serve_id).order_by("-created_at").all()

    async def get_latest_artifact_by_serve_id(self, serve_id: int) -> Artifact:
        return await Artifact.filter(serve_id=serve_id).order_by("-created_at").first()

    async def check_artifact_builder_job_exists(self, serve_id: int, version: str) -> bool:
        return await (ArtifactBuilderJob
                      .filter(serve_id=serve_id,
                              version=version,
                              status__in=[JobStatus.PENDING.value, JobStatus.SUCCESSFUL.value]
                              )
                      .exists()
                      )

    async def check_version_already_exists(self, serve_id: int, version: str) -> bool:
        return await Artifact.filter(serve_id=serve_id, version=version).exists()

    async def get_artifact_by_version(self, serve_id: int, version: str) -> Optional[Artifact]:
        return await Artifact.filter(serve_id=serve_id, version=version).first()

    async def get_artifact_builder_job_status(self, artifact_id):
        return await ArtifactBuilderJob.filter(id=artifact_id)

    async def list_artifact_builder_jobs(
            self,
            serve_name: Optional[str] = None,
            status: Optional[str] = None,
    ):
        query = ArtifactBuilderJob.all()
        if serve_name:
            query = query.filter(serve__name=serve_name)
        if status:
            query = query.filter(status=status)
        # Return plain dicts including serve name for convenience
        return await query.order_by("-created_at").values(
            "id",
            "version",
            "status",
            "task_id",
            "created_at",
            "status_last_updated_at",
            "serve_id",
            "serve__name",
        )

    async def get_unpicked_artifact_builder_job(self) -> Optional[ArtifactBuilderJob]:
        async with in_transaction():
            artifact_builder_job = await ArtifactBuilderJob.raw(GET_UNPICKED_ARTIFACTS_QUERY)

            if not artifact_builder_job:
                return None

            artifact_builder_job[0].last_picked_at = datetime.now(timezone.utc)
            await artifact_builder_job[0].save()
            return artifact_builder_job[0]

    async def create_artifact_and_update_job_status(
            self,
            artifact_builder_job: ArtifactBuilderJob,
            status: JobStatus
    ) -> None:
        async with in_transaction():
            serve = await artifact_builder_job.serve
            user = await artifact_builder_job.created_by
            if status == JobStatus.SUCCESSFUL:
                await Artifact.create(
                    serve=serve,
                    version=artifact_builder_job.version,
                    github_repo_url=artifact_builder_job.github_repo_url,
                    branch=artifact_builder_job.branch,
                    artifact_job=artifact_builder_job,
                    created_by=user,
                    image_url=f"{CONTAINER_REGISTRY}/{IMAGE_REPOSITORY}:{serve.name}_{artifact_builder_job.version}",
                )
            artifact_builder_job.status = status.value
            artifact_builder_job.status_last_updated_at = datetime.now(timezone.utc)
            await artifact_builder_job.save()
