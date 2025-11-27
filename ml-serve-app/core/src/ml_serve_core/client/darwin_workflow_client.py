from ml_serve_core.client.http_client import AsyncHttpClient
from ml_serve_core.config.configs import Config
from ml_serve_model import User


class DarwinWorkflowClient:

    def __init__(self):
        self.config = Config()

    async def create_workflow_serve(
            self,
            darwin_workflow_url: str,
            data: dict,

    ):
        url = darwin_workflow_url + "/v2/workflow"
        async with AsyncHttpClient() as client:
            resp = await client.post(url, data=data)
            return resp

    async def update_workflow_serve(
            self,
            darwin_workflow_url: str,
            workflow_id: str,
            data: dict
    ):
        url = darwin_workflow_url + f"/v2/workflow/{workflow_id}"
        async with AsyncHttpClient() as client:
            resp = await client.put(url, data=data)
            return resp

    async def create_job_cluster_definition(
            self,
            darwin_workflow_url: str,
            data: dict
    ):
        url = darwin_workflow_url + "/job-cluster-definitions"
        async with AsyncHttpClient() as client:
            resp = await client.post(url, data=data)
            return resp

    async def update_job_cluster_definition(
            self,
            darwin_workflow_url: str,
            job_cluster_definition_id: str,
            data: dict
    ):
        url = darwin_workflow_url + f"/job-cluster-definitions/{job_cluster_definition_id}"
        async with AsyncHttpClient() as client:
            resp = await client.put(url, data=data)
            return resp

    async def get_job_cluster_definition(
            self,
            darwin_workflow_url: str,
            job_cluster_definition_id: str
    ):
        url = darwin_workflow_url + f"/job-cluster-definitions/{job_cluster_definition_id}"
        async with AsyncHttpClient() as client:
            resp = await client.get(url)
            return resp['data']['data']

    async def get_workflow_id_by_name(
            self,
            darwin_workflow_url: str,
            workflow_name: str,
    ):
        url = darwin_workflow_url + f"/workflow_id"
        data = {
            "workflow_name": workflow_name
        }
        async with AsyncHttpClient() as client:
            resp = await client.post(url, data=data)
            if resp['status_code'] == 404:
                return None
            return resp['data']['workflow_id']

    async def get_workflow_by_id(
            self,
            darwin_workflow_url: str,
            workflow_id: str,
    ):
        url = darwin_workflow_url + f"/workflow/{workflow_id}"
        async with AsyncHttpClient() as client:
            resp = await client.get(url)
            return resp['data']['data']
