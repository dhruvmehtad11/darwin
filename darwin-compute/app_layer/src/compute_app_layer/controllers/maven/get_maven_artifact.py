from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.service.maven_service import MavenService


async def get_maven_artifact_versions_controller(group_id: str, artifact_id: str, maven: MavenService):
    try:
        resp = await maven.get_maven_artifact_versions(group_id=group_id, artifact_id=artifact_id)

        logger.debug(f"[Maven] Fetched versions for {artifact_id}: {resp.versions}")

        return Response.success_response(
            data=resp, message=f"[Maven] Fetched versions for {artifact_id}: {resp.versions}"
        )

    except Exception as e:
        logger.error(f"[Maven] Error fetching versions for {artifact_id} : {e}")
        return Response.internal_server_error_response(
            message=f"[Maven] Error fetching versions for {artifact_id} - {e.__str__()}", data=None
        )
