from fastapi import HTTPException
from starlette.responses import JSONResponse

from ml_serve_app_layer.utils.response_util import Response


def handle_exception(exception: Exception) -> JSONResponse:
    return Response.internal_server_error_response(exception.__str__())
