import json
from typing import List

import requests

from workspace_app_layer.constants.config import Config
from workspace_app_layer.models.workspace.get_all_cluster_request import GetAllClusterRequest
from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


class Compute:
    """
    Proxy Class for interacting with Darwin Compute
    """

    def __init__(self, env: str):
        self.env = env
        self.client = Config(env).darwin_compute_url

    @staticmethod
    def _request(method: str, url: str, params: dict = None, data: dict = None, headers=None):
        response = requests.request(method, url, params=params, json=data, headers=headers, timeout=600)
        response_json = response.json()
        logger.debug(f"Response from Darwin Compute: {url} {response_json}")
        if not 200 <= response.status_code < 300:
            raise Exception(response_json["data"])
        if response_json["status"] != "SUCCESS":
            raise Exception(response_json["data"])
        return response_json.get("data")

    @staticmethod
    def _request_listing(method: str, url: str, params: dict = None, data: dict = None, headers=None):
        response = requests.request(method, url, params=params, json=data, headers=headers, timeout=600)
        response_json = response.json()
        logger.debug(f"Response from Darwin Compute: {url} {response_json}")
        if not 200 <= response.status_code < 300:
            raise Exception(response_json["data"])
        if response_json["status"] != "SUCCESS":
            raise Exception(response_json["data"])
        return response_json

    def healthcheck(self) -> bool:
        """
        Healthcheck for Darwin Compute
        Returns:
            bool: True if healthy
        """
        url = self.client + "/health"
        self._request("GET", url)
        return True

    def start_cluster(self, cluster_id: str, user: str = "darwin-workspace") -> dict:
        """
        Start a cluster
        Args:
            cluster_id: Cluster Identification
            user: User who is starting the cluster
        Returns:
            str: Active/Inactive
        """
        url = self.client + f"/cluster/start-cluster/{cluster_id}"
        headers = {"msd-user": json.dumps({"email": user})}
        resp = self._request("POST", url, headers=headers)
        logger.debug(f"Cluster start response for {cluster_id} : {resp}")
        return resp

    def get_cluster_details(self, cluster_id: str) -> dict:
        """
        Get cluster details
        Args:
            cluster_id: Cluster Identification
        Returns:
            dict: cluster details
        """
        url = self.client + f"/cluster/{cluster_id}"
        resp = self._request("GET", url)
        return resp

    def get_cluster_metadata(self, cluster_id: str) -> dict:
        """
        Get cluster metadata
        Args:
            cluster_id: Cluster Identification
        Returns:
            dict: Cluster Metadata
        """
        url = self.client + f"/cluster/{cluster_id}/metadata"
        resp = self._request("GET", url)
        return resp

    def get_cluster_dashboards(self, cluster_id: str, internal: bool = False) -> dict:
        """
        Get cluster dashboards
        Args:
            cluster_id: Cluster Identification
            internal: True if internal dashboard
        Returns:
            dict: Cluster Dashboards
        """
        url = self.client + f"/cluster/{cluster_id}/dashboards"
        params = {"internal": internal}
        resp = self._request("GET", url, params)
        return resp

    def get_all_clusters_metadata(self, request: GetAllClusterRequest) -> List[dict]:
        """
        Get all cluster metadata
        Returns:
            str: List of clusters metadata
        """
        url = self.client + "/search"
        data = {
            "query": request.search_string,
            "filters": {},
            "page_size": request.page_size,
            "offset": request.offset,
            "sort_by": "created_on",
            "sort_order": "desc",
        }
        resp = self._request_listing("POST", url, data=data)
        return resp

    def get_jupyter_client(self, workspace_id: int, codespace_id: int, suffix: str = None) -> str:
        url = self.client + "/jupyter-pod"
        params = {"consumer_id": f"{workspace_id}-{codespace_id}"}

        resp = self._request("POST", url, data=params)
        return resp["jupyterLink"] + suffix if suffix else resp["jupyterLink"]

    def restart_jupyter_client(self, cluster_id: str, release_name: str, namespace: str):
        url = self.client + "/jupyter/restart"
        jupyter_path = f"http://{cluster_id}-kuberay-head-svc.{namespace}.svc.cluster.local:8888"
        data = {
            "jupyter_path": jupyter_path,
            "release_name": release_name,
        }

        resp = self._request("POST", url, data=data)
        return resp

    def get_jupyter_client_path(self, release_name: str):
        url = self.client + "/jupyter/get-path"
        data = {
            "release_name": release_name,
        }

        resp = self._request("GET", url, data=data)
        return resp
