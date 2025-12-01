import traceback

from loguru import logger

from ml_serve_core.client.artifact_builder_client import ArtifactBuilderClient
from ml_serve_core.service.artifact_service import ArtifactService

from ml_serve_model.artifact_builder_job import JobStatus
from ml_serve_model.enums import JobStatus


class ArtifactStatusPoller:
    def __init__(self):
        self.artifact_service = ArtifactService()
        self.artifact_builder_client = ArtifactBuilderClient()

    async def get_and_update_artifact_status(self):
        try:
            artifact_builder_job = await self.artifact_service.get_unpicked_artifact_builder_job()
            if not artifact_builder_job:
                return

            logger.info(f"Picked artifact builder job: {artifact_builder_job.id}")

            # Update the status of the artifact from the artifact builder
            build_status = await self.artifact_builder_client.get_artifact_status(artifact_builder_job.task_id)

            if build_status in ["waiting", "running"]:
                build_status = JobStatus.PENDING
            elif build_status == "failed":
                build_status = JobStatus.FAILED
            elif build_status == "completed":
                build_status = JobStatus.SUCCESSFUL

            await self.artifact_service.create_artifact_and_update_job_status(artifact_builder_job, build_status)
        except Exception as e:
            tb = traceback.format_exc()
            logger.exception(f"Error in getting and updating artifact status: {e} - {tb}")
            raise e




