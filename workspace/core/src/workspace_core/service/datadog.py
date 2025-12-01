from workspace_app_layer.models.workspace.datadog_query_request import DatadogQueryRequest
from workspace_app_layer.utils.utils import _request
from workspace_core.constants.constants import DATADOG_API_URL, DATADOG_APP_KEY, DATADOG_API_KEY

from workspace_core.utils.logging_util import get_logger

logger = get_logger(__name__)


class DataDog:
    def __init__(self):
        self.client = DATADOG_API_URL

    def fetch_datadog_metrics(self, request: DatadogQueryRequest) -> dict:
        params = {
            "api_key": DATADOG_API_KEY,
            "application_key": DATADOG_APP_KEY,
            "from": request.unix_start_time,
            "to": request.unix_end_time,
            "query": request.datadog_query,
        }
        logger.debug(f"Fetching Datadog metrics with params: {params}")
        response = _request(method="GET", url=self.client, params=params)
        return response
