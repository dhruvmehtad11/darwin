import json
import requests
from fastapi import Request
from fastapi.responses import JSONResponse

from mlflow_app_layer.constant.config import Config


async def create_user_controller(request: Request, config: Config):
    msd_user = json.loads(request.headers.get("msd-user"))
    username = msd_user["email"]

    user_details_response = requests.get(
        f"{config.mlflow_app_layer_url}/api/2.0/mlflow/users/get?username={username}",
        auth=("admin", "password"),
    )
    if user_details_response.status_code == 200:
        return JSONResponse(
            content={"status": "SUCCESS", "message": "User already exists"},
            status_code=200,
        )

    user_creation_response = requests.post(
        f"{config.mlflow_app_layer_url}/api/2.0/mlflow/users/create",
        json={"username": username, "password": username},
        auth=("admin", "password"),
    )

    user_role_change_response = requests.patch(
        f"{config.mlflow_app_layer_url}/api/2.0/mlflow/users/update-admin",
        json={"username": username, "is_admin": True},
        auth=("admin", "password"),
    )

    if (
        user_creation_response.status_code == 200
        and user_role_change_response.status_code == 200
    ):
        return JSONResponse(
            content={"status": "SUCCESS", "message": "User created successfully"},
            status_code=200,
        )

    return JSONResponse(
        content={"status": "FAILED", "message": "User creation failed"}, status_code=500
    )
