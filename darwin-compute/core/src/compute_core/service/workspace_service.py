from compute_core.constant.config import Config
from compute_core.service.utils import make_api_request


class WorkspaceService:
    """
    Proxy Class for interacting with Cluster Manager
    """

    def __init__(self, env: str = None):
        _config = Config(env)
        self.client = _config.get_workspace_app_layer_url

    def upload_file_to_s3(self, path: str, s3_bucket: str, destination_path: str):
        s3_bucket_name = s3_bucket.split("/")[2]
        destination_folder_name = s3_bucket.split("/")[3]
        response = make_api_request(
            "POST",
            url=f"{self.client}/upload_to_s3",
            data={
                "source_path": path,
                "s3_bucket": s3_bucket_name,
                "destination_path": f"{destination_folder_name}/{destination_path}",
            },
        )
        return response
