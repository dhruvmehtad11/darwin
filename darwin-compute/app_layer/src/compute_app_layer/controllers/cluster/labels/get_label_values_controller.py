from loguru import logger

from compute_app_layer.models.request.labels import LabelValuesRequest
from compute_app_layer.utils.response_util import Response
from compute_core.dao.async_cluster_dao import get_labels_values


async def get_label_values_controller(request: LabelValuesRequest):
    """
    Get available values for a list of label keys.
    :args: request: LabelValuesRequest containing the list of label keys
    :return: Response containing available values for each label key
    """
    try:
        label_keys = request.keys

        logger.debug(f"Getting label values for keys: {label_keys}")

        result = await get_labels_values(request.keys)

        data = {"keys": label_keys, "values": result}

        return Response.success_response("Label values retrieved successfully", data)

    except Exception as e:
        logger.exception(f"Failed to get label values for keys {request.keys}: {e}")
        return Response.internal_server_error_response(
            "Failed to get label values", {"error": str(e), "label_keys": request.keys}
        )
