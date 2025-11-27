from fastapi import APIRouter

from compute_app_layer.controllers.maven.get_maven_artifact import get_maven_artifact_versions_controller
from compute_app_layer.controllers.maven.get_maven_library import get_maven_library_controller
from compute_app_layer.models.request.maven_search import MavenRepository
from compute_core.service.maven_service import MavenService

router = APIRouter(prefix="/maven")

maven_service = MavenService()


@router.get("/package")
async def get_maven_library(repository: MavenRepository, search: str, page_size: int, offset: int):
    return await get_maven_library_controller(
        repository=repository, search=search, page_size=page_size, offset=offset, maven=maven_service
    )


@router.get("/package/group/{group_id}/artifact/{artifact_id}")
async def get_maven_artifact_versions(group_id: str, artifact_id: str):
    return await get_maven_artifact_versions_controller(group_id=group_id, artifact_id=artifact_id, maven=maven_service)
