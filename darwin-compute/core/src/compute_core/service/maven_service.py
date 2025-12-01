from loguru import logger
from async_lru import alru_cache

from compute_app_layer.models.request.maven_search import MavenRepository
from compute_core.constant.constants import MAVEN_CENTRAL_BASE_URL
from compute_core.dto.maven_dto import MavenLibrary, MavenArtifacts
from compute_core.service.utils import make_async_api_request
from compute_core.util.utils import format_maven_response


class MavenService:

    @alru_cache(maxsize=10000)
    async def get_library(
        self, repository: MavenRepository, search_query: str, page_size: int, offset: int
    ) -> MavenLibrary:
        """
        Args:
            repository: MavenRepository
            search_query: str
            page_size: int
            offset: int
        Returns:
            MavenLibrary class with maven artifact response
        """
        try:
            params = {"q": search_query, "rows": page_size, "start": offset, "wt": "json"}

            maven_base_url = MAVEN_CENTRAL_BASE_URL
            logger.debug(f"Sending maven request to {maven_base_url} with params {params}")
            maven_response = await make_async_api_request(
                method="GET", url=maven_base_url, params=params, timeout=6, max_retries=3
            )
            resp = format_maven_response(
                maven_resp=maven_response["response"]["docs"], num_found=maven_response["response"]["numFound"]
            )
            logger.debug(f"Fetched maven response in maven service get_library from api- {maven_base_url}")
            return resp

        except Exception as e:
            logger.error(f"Error occurred in maven service get_library - {str(e)}")
            raise e

    async def get_all_maven_artifact_version_response(self, method: str, url: str, query: str) -> list[dict]:
        try:
            offset = 0
            rows = 200
            maven_resp = []
            while True:
                maven_artifact_version_url = f"{url}?q={query}&rows={rows}&start={offset}&wt=json&core=gav"

                logger.info(
                    f"Fetching maven artifact versions with query - {query} from api- {maven_artifact_version_url} and current offset - {offset}"
                )

                resp = await make_async_api_request(
                    method=method, url=maven_artifact_version_url, timeout=6, max_retries=3
                )

                if len(resp["response"]["docs"]) == 0:
                    break
                maven_resp.extend(resp["response"]["docs"])

                offset += rows
            logger.debug(f"Fetched all maven response from api- {url} ")
            return maven_resp
        except Exception as e:
            logger.error(f"Error occurred in get_all_maven_response - {str(e)}")
            raise e

    @alru_cache(maxsize=10000)
    async def get_maven_artifact_versions(self, group_id: str, artifact_id: str) -> MavenArtifacts:
        try:
            logger.debug(
                f"Fetching maven artifact versions for {artifact_id} in maven service get_maven_artifact_versions"
            )

            maven_base_url = MAVEN_CENTRAL_BASE_URL

            artifact_version_query = f"g:{group_id}+AND+a:{artifact_id}"

            maven_response = await self.get_all_maven_artifact_version_response(
                method="GET", url=maven_base_url, query=artifact_version_query
            )

            formatted_maven_resp = MavenArtifacts(
                group_id=group_id, artifact_id=artifact_id, versions=[item["v"] for item in maven_response]
            )

            logger.debug(
                f"Successfully fetched all artifact versions for {artifact_id} in maven service get_maven_artifact_versions"
            )
            return formatted_maven_resp
        except Exception as e:
            logger.error(f"Error occurred in get_maven_artifact_versions - {str(e)}")
            raise e
