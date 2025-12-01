from fastapi import APIRouter
from compute_app_layer.controllers.default_cluster_logs import default_cluster_logs_controller

router = APIRouter(prefix="/events")


@router.get("/default-logs")
async def get_default_events():
    return await default_cluster_logs_controller.get_default_events()
