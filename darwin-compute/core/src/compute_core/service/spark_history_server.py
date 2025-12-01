"""
This module contains the class for Spark History Server
"""

import requests
from loguru import logger

from compute_core.constant.config import Config
from compute_core.util.utils import urljoin


class SparkHistoryServerService:
    """
    Proxy Class for interacting with SparkHistoryServer
    """

    def __init__(self, env: str = None):
        self._config = Config(env)

    @staticmethod
    def _request(method: str, url: str, params: dict = None, data: dict = None):
        response = requests.request(method, url, params=params, json=data, timeout=30)
        if not 200 <= response.status_code < 300:
            logger.exception(f"Error occurred in API {method} - {url} - {response.text}, body - {data}")
            raise Exception(f"Error occurred in API {method} - {url} - {response.text}")
        return response.json() if response.headers.get("content-type") == "application/json" else response.text

    def is_url_active(self, shs_id: str, cloud_env: str) -> bool:
        try:
            self._request("GET", self.get_url(shs_id, cloud_env, True))
            return True
        except Exception as e:
            logger.debug(f"Spark History Server {shs_id} is inactive - {e}")
            return False

    def get_url(self, shs_id: str, cloud_env: str, internal: bool = False) -> str:
        host_url = self._config.host_url if not internal else self._config.internal_host_url(cloud_env)
        shs_url = urljoin(host_url, cloud_env, shs_id, "/", https=False if internal else True)
        return shs_url
