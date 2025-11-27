from fastapi import APIRouter, Depends

from compute_app_layer.controllers.spark_history_server import spark_history_server_controller
from compute_app_layer.models.spark_history_server import SparkHistoryServer
from compute_app_layer.routers.dependency_cache import get_shs_dao, get_shs_service, get_dcm, get_config
from compute_core.constant.config import Config
from compute_core.dao.spark_history_server_dao import SparkHistoryServerDAO
from compute_core.service.dcm import DarwinClusterManager
from compute_core.service.spark_history_server import SparkHistoryServerService

router = APIRouter(prefix="/spark-history-server")


@router.get("/")
async def get_all_spark_history_servers(
    limit: int, offset: int, mysql_dao: SparkHistoryServerDAO = Depends(get_shs_dao)
):
    return await spark_history_server_controller.get_all(limit, offset, mysql_dao)


@router.get("/active")
async def get_active_spark_history_servers(
    limit: int, offset: int, mysql_dao: SparkHistoryServerDAO = Depends(get_shs_dao)
):
    return await spark_history_server_controller.get_active(limit, offset, mysql_dao)


@router.get("/{spark_history_server_id}")
async def get_spark_history_server_by_id(
    spark_history_server_id: str,
    mysql_dao: SparkHistoryServerDAO = Depends(get_shs_dao),
    shs: SparkHistoryServerService = Depends(get_shs_service),
):
    return await spark_history_server_controller.get_by_id(spark_history_server_id, mysql_dao, shs)


@router.get("/resource/{resource}")
async def get_spark_history_server_by_resource(
    resource: str,
    mysql_dao: SparkHistoryServerDAO = Depends(get_shs_dao),
    shs: SparkHistoryServerService = Depends(get_shs_service),
):
    return await spark_history_server_controller.get_by_resource(resource, mysql_dao, shs)


@router.post("/")
async def create_spark_history_server(
    req: SparkHistoryServer,
    mysql_dao: SparkHistoryServerDAO = Depends(get_shs_dao),
    dcm: DarwinClusterManager = Depends(get_dcm),
    config: Config = Depends(get_config),
):
    return await spark_history_server_controller.create(req, mysql_dao, dcm, config)


@router.put("/stop")
async def stop_spark_history_server(
    spark_history_server_id: str,
    mysql_dao: SparkHistoryServerDAO = Depends(get_shs_dao),
    dcm: DarwinClusterManager = Depends(get_dcm),
    config: Config = Depends(get_config),
):
    return await spark_history_server_controller.stop(spark_history_server_id, mysql_dao, dcm, config)


@router.delete("/{spark_history_server_id}")
async def delete_spark_history_server(
    spark_history_server_id: str, mysql_dao: SparkHistoryServerDAO = Depends(get_shs_dao)
):
    return await spark_history_server_controller.delete(spark_history_server_id, mysql_dao)
