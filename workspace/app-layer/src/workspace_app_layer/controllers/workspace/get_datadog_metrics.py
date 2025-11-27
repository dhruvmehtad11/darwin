from starlette.responses import JSONResponse

from workspace_app_layer.models.workspace.datadog_query_request import DatadogQueryRequest
from workspace_app_layer.utils.response_utils import Response
from workspace_core.utils.logging_util import get_logger
from workspace_app_layer.utils.utils import error_handler

logger = get_logger(__name__)


async def datadog_metrics_controller(datadog_client, request: DatadogQueryRequest) -> JSONResponse:
    try:
        datadog_metric_response = datadog_client.fetch_datadog_metrics(request)
        logger.info(f"Successfully fetched datadog metrics : {datadog_metric_response}")
        return Response.success_response(
            data=datadog_metric_response,
            message=f"Successfully fetched datadog metrics with query: {request.datadog_query}",
        )

    except Exception as err:
        logger.exception(f"Error fetching metrics {err}")
        return error_handler(err.__str__())
