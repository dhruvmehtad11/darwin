import traceback

from ml_serve_app_layer.dtos.requests import CreateArtifactRequest
from ml_serve_core.client.http_client import AsyncHttpClient
from ml_serve_core.config.configs import Config
from ml_serve_core.utils.utils import validate_git_branch_exists
from ml_serve_model import Serve
from loguru import logger


class ArtifactBuilderClient:
    def __init__(self):
        self.config = Config()

    async def create_artifact(
            self,
            request: CreateArtifactRequest,
            serve: Serve
    ):
        try:
            branch = request.branch if request.branch else "main"
            
            # Validate if the branch exists before proceeding
            if not validate_git_branch_exists(request.github_repo_url, branch):
                raise Exception(f"Branch '{branch}' does not exist in repository {request.github_repo_url}")
            
            async with AsyncHttpClient() as client:
                url = f"{self.config.get_artifact_builder_url}/build_with_dockerfile"
                params = {
                    "app_name": serve.name,
                    "image_tag": f"{serve.name}_{request.version}",
                    "git_repo": request.github_repo_url,
                    "branch": branch
                }

                resp = await client.post(url, data=params)
                print(resp)
                return resp["body"]
        except Exception as e:
            tb = traceback.format_exc()
            logger.error(f"Error while creating artifact: {e} with traceback: {tb}")
            raise Exception(f"Error while creating artifact: {str(e)}")

    async def get_artifact_status(self, task_id):
        try:
            async with AsyncHttpClient() as client:
                url = f"{self.config.get_artifact_builder_url}/task/status"
                params = {
                    "task_id": task_id
                }

                resp = await client.get(url, params)
                return resp["body"]["build_status"]
        except Exception as e:
            tb = traceback.format_exc()
            logger.error(f"Error while getting artifact status: {e} with traceback: {tb}")
            raise Exception("Error while getting artifact status")
