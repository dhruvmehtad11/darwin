from local.src.mock_services.models.requests.mock_requests import JupyterPodRequest, SearchRequest
from local.src.mock_services.utils.utils import (
    mock_cluster_details,
    mock_cluster_dashboards,
    return_response,
    mock_jupyter_data,
    mock_cluster_start,
    mock_data_with_summary,
)
from fastapi import FastAPI

mock_app = FastAPI()


@mock_app.get("/mock/health")
def health():
    return "Mock service is up and running"


@mock_app.get("/mock/{mock_object}")
def get_response_for_mock_object(mock_object: str):
    return return_response(mock_data_with_summary())


@mock_app.get("/mock/cluster/start-cluster/{cluster_id}")
def start_cluster(cluster_id: str):
    return return_response(mock_cluster_start())


@mock_app.get("/mock/cluster/{cluster_id}")
def get_cluster_details(cluster_id: str):
    return return_response(mock_cluster_details())


@mock_app.get("/mock/cluster/{cluster_id}/dashboards")
def get_dashboards(cluster_id: str):
    return return_response(mock_cluster_dashboards())


@mock_app.post("/mock/search")
def get_all_cluster_metadata(request: SearchRequest):
    return return_response(mock_cluster_details(), is_data_list=True)


@mock_app.post("/mock/jupyter-pod")
def get_jupyter_client(request: JupyterPodRequest):
    return return_response(mock_jupyter_data())
