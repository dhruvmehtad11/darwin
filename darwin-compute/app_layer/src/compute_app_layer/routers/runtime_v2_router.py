import json

from fastapi import APIRouter, Depends, Header
from loguru import logger

from compute_app_layer.controllers.runtime_v2.create_runtime_controller import create_runtime_controller
from compute_app_layer.controllers.runtime_v2.get_runtime_details_controller import get_runtime_details_controller
from compute_app_layer.controllers.runtime_v2.get_runtimes_controller import get_runtimes_controller
from compute_app_layer.controllers.runtime_v2.soft_delete_runtime_controller import soft_delete_runtime_controller
from compute_app_layer.controllers.runtime_v2.update_runtime_controller import update_runtime_controller
from compute_app_layer.models.runtime_v2 import RuntimeV2Request, GetRuntimesRequest
from compute_app_layer.utils.response_util import Response
from compute_core.runtime_v2 import RuntimeV2


router = APIRouter(prefix="/runtime/v2")


@router.post("/create")
async def create_runtime(request: RuntimeV2Request, runtimev2: RuntimeV2 = Depends(RuntimeV2)):
    # This route inserts a runtime in DB
    # Note - for a soft deleted runtime, it will execute an update query in the backend instead of creating a new runtime but, to the end user, it'll appear as if a new runtime has been created
    return await create_runtime_controller(runtimev2=runtimev2, request=request)


@router.post("/get-all")
async def get_runtimes(
    # This route is used to get all the runtimes from DB
    request: GetRuntimesRequest,
    user_data: str = Header(..., alias="msd-user"),
    runtimev2: RuntimeV2 = Depends(RuntimeV2),
):
    try:
        user_data = json.loads(user_data)  # Convert string to dict
        user = user_data.get("email")  # Extract email
    except (json.JSONDecodeError, AttributeError) as e:
        # Handle missing or malformed header
        logger.exception(f"Invalid user data in the header, Error: {e.__str__()}")
        return Response.bad_request_error_response(
            message=f"Invalid user data in the header, Error: {e.__str__()}", data=None
        )
    return await get_runtimes_controller(runtimev2=runtimev2, request=request, user=user)


@router.get("/get-details/{runtime}")
async def get_runtime_details(runtime: str, runtimev2: RuntimeV2 = Depends(RuntimeV2)):
    # This route is used to get the details of a runtime from DB
    return await get_runtime_details_controller(runtimev2=runtimev2, runtime=runtime)


@router.post("/update")
async def update_runtime(request: RuntimeV2Request, runtimev2: RuntimeV2 = Depends(RuntimeV2)):
    # This route updates a runtime already present in the DB by name.
    # Note - You can’t update name - instead create a new runtime with the new name and delete the old one
    return await update_runtime_controller(runtimev2=runtimev2, request=request)


@router.delete("/soft-delete/{runtime}")
async def soft_delete_runtime(runtime: str, runtimev2: RuntimeV2 = Depends(RuntimeV2)):
    # This route is used to soft delete the runtime i.e. sets is_deleted=True for that runtime in the DB
    return await soft_delete_runtime_controller(runtimev2=runtimev2, runtime=runtime)
