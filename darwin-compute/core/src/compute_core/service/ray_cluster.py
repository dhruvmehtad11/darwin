from compute_core.service.utils import make_api_request, make_async_api_request


class RayClusterService:
    def __init__(self, ray_dashboard: str):
        self.ray_dashboard = ray_dashboard

    def get_summary(self):
        url = f"{self.ray_dashboard}nodes?view=summary"
        nodes_response = make_api_request(method="GET", url=url, timeout=2, max_retries=3)
        nodes = nodes_response["data"]["summary"]
        return nodes


class AsyncRayClusterService:
    """Async version of RayClusterService for better concurrency in batch operations"""

    def __init__(self, ray_dashboard: str):
        self.ray_dashboard = ray_dashboard

    async def get_summary(self):
        url = f"{self.ray_dashboard}nodes?view=summary"
        nodes_response = await make_async_api_request(method="GET", url=url, timeout=2, max_retries=3)
        nodes = nodes_response["data"]["summary"]
        return nodes
