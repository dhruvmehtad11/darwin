import json
import requests
from fastapi import Request
from fastapi.responses import JSONResponse

from mlflow_app_layer.constant.config import Config


async def create_user_controller(request: Request, config: Config):
    # TODO: for now mlflow doesn't have any auth support so skipping it.
    return JSONResponse(
            content={"status": "SUCCESS", "message": "User created successfully"},
            status_code=200,
        )
