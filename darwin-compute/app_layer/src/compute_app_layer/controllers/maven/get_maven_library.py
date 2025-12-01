from loguru import logger

from compute_app_layer.models.request.maven_search import MavenRepository
from compute_app_layer.utils.response_util import Response
from compute_core.service.maven_service import MavenService


async def get_maven_library_controller(
    repository: MavenRepository, search: str, page_size: int, offset: int, maven: MavenService
):
    try:
        if page_size > 200:
            logger.error(f"[Maven] Error: Page size cannot be greater than 200")
            raise Exception("Page size cannot be greater than 200")

        resp = await maven.get_library(repository=repository, search_query=search, page_size=page_size, offset=offset)

        logger.debug(f"[Maven] Fetched query: {search}")

        return Response.success_response(data=resp, message=f"[Maven] Fetched query: {search}")
    except Exception as e:
        logger.error(f"[Maven] Error fetching query - {search}, Error: {e.__str__()}")
        return Response.internal_server_error_response(
            message=f"[Maven] Error fetching query - {e.__str__()}", data=None
        )
