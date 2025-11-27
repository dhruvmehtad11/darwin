from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.dto.exceptions import (
    ClusterNotFoundError,
    ClusterRunIdNotFoundError,
    ExecutionNotFoundError,
    LibraryNotFoundError,
)


class ResponseHandler:
    def __init__(self):
        self.response = None

    def __enter__(self):
        # No setup needed; simply return self
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        # If no exception occurs, do nothing
        if exc_type is None:
            return False

        # Handle specific exceptions
        if exc_type is ClusterNotFoundError:
            logger.exception(f"Cluster not found :{exc_value}")
            self.response = Response.not_found_error_response(message=str(exc_value), data=exc_value)
        elif exc_type is ClusterRunIdNotFoundError:
            logger.exception(f"Cluster run id not found: {exc_value}")
            self.response = Response.not_found_error_response(message=str(exc_value), data=exc_value)
        elif exc_type is ExecutionNotFoundError:
            logger.exception(f"Execution not found: {exc_value}")
            self.response = Response.not_found_error_response(message=str(exc_value), data=exc_value)
        elif exc_type is LibraryNotFoundError:
            logger.exception(f"Library not found: {exc_value}")
            self.response = Response.not_found_error_response(message=str(exc_value), data=exc_value)
        else:
            logger.exception(f"Unexpected error: {exc_value}")
            self.response = Response.internal_server_error_response(message="Internal Server Error", data=exc_value)

        # Return True to suppress the exception propagation
        return True
