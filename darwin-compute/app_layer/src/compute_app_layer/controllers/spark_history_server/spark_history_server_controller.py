from fastapi.responses import JSONResponse
from loguru import logger

from compute_app_layer.models.spark_history_server import (
    SparkHistoryServer,
    SparkHSStatus,
)
from compute_app_layer.utils.response_util import Response
from compute_core.constant.config import Config
from compute_core.dao.spark_history_server_dao import SparkHistoryServerDAO
from compute_core.service.dcm import DarwinClusterManager
from compute_core.service.spark_history_server import SparkHistoryServerService


async def get_all(limit: int, offset: int, mysql_dao: SparkHistoryServerDAO) -> JSONResponse:
    try:
        logger.debug(f"Getting all spark history servers from {offset} to {limit + offset}")
        resp = mysql_dao.get_all(limit, offset)
        return Response.success_response(
            f"All Spark History Servers from {offset} to {limit + offset} Fetched Successfully", resp
        )
    except Exception as e:
        logger.exception(f"Error getting all spark history servers: {e}")
        return Response.internal_server_error_response("Error getting all spark history servers", e.__str__())


async def get_active(limit: int, offset: int, mysql_dao: SparkHistoryServerDAO) -> JSONResponse:
    try:
        logger.debug("Getting all active spark history servers")
        resp = mysql_dao.get_all_active(limit, offset)
        return Response.success_response("All Active Spark History Servers Fetched Successfully", resp)
    except Exception as e:
        logger.exception(f"Error getting active spark history servers: {e}")
        return Response.internal_server_error_response("Error getting active spark history servers", e.__str__())


async def get_by_id(
    spark_history_server_id: str, mysql_dao: SparkHistoryServerDAO, shs: SparkHistoryServerService
) -> JSONResponse:
    try:
        logger.debug(f"Getting spark history server by ID: {spark_history_server_id}")
        shs_data = mysql_dao.get_by_id(spark_history_server_id)
        if not shs_data:
            logger.error(f"Spark History Server Not Found: {spark_history_server_id}")
            return Response.not_found_error_response(
                "Spark History Server Not Found", {"server_id": spark_history_server_id}
            )
        if shs_data.status == SparkHSStatus.CREATED and shs.is_url_active(spark_history_server_id, shs_data.cloud_env):
            shs_data.status = SparkHSStatus.ACTIVE
            mysql_dao.update_status(spark_history_server_id, shs_data.status)
        shs_data.url = shs.get_url(spark_history_server_id, shs_data.cloud_env)
        return Response.success_response("Spark History Server Fetched Successfully", shs_data)
    except Exception as e:
        logger.exception(f"Error getting spark history server by ID: {spark_history_server_id}: {e}")
        return Response.internal_server_error_response("Error getting spark history server by ID", e.__str__())


async def get_by_resource(
    resource: str, mysql_dao: SparkHistoryServerDAO, shs: SparkHistoryServerService
) -> JSONResponse:
    try:
        logger.debug(f"Getting spark history server by resource: {resource}")
        shs_data = mysql_dao.get_by_resource(resource)
        if not shs_data:
            logger.error(f"Spark History Server Not Found: {resource}")
            return Response.not_found_error_response("Spark History Server Not Found", {"resource": resource})
        if shs_data.status == SparkHSStatus.CREATED and shs.is_url_active(shs_data.id, shs_data.cloud_env):
            shs_data.status = SparkHSStatus.ACTIVE
            mysql_dao.update_status(shs_data.id, shs_data.status)
        shs_data.url = shs.get_url(shs_data.id, shs_data.cloud_env)
        return Response.success_response("Spark History Server Fetched Successfully", shs_data)
    except Exception as e:
        logger.exception(f"Error getting spark history server by resource: {resource}: {e}")
        return Response.internal_server_error_response("Error getting spark history server by resource", e.__str__())


async def create(
    request: SparkHistoryServer, mysql_dao: SparkHistoryServerDAO, dcm: DarwinClusterManager, config: Config
) -> JSONResponse:
    try:
        logger.debug(f"Creating spark history server: {request}")
        namespace = config.default_namespace
        existing_hs = mysql_dao.get_by_resource(request.resource)
        if existing_hs:
            logger.debug(f"Existing spark history server: {existing_hs}")
            request.id = existing_hs.id
            if existing_hs.status in [SparkHSStatus.CREATED, SparkHSStatus.ACTIVE]:
                return Response.success_response("Spark History Server Already Exists", {"server_id": request.id})
            else:
                logger.debug(f"Updating existing spark history server: {request}")
                if not request.cloud_env:
                    request.cloud_env = mysql_dao.get_default_cloud_env()
                resp = mysql_dao.update(request)
                kube_cluster = config.get_kube_cluster(request.cloud_env)
                dcm.start_spark_history_server(request, kube_cluster, namespace)
                return Response.success_response("Spark History Server Updated Successfully", {"server_id": resp})
        if not request.cloud_env:
            request.cloud_env = mysql_dao.get_default_cloud_env()
        resp = mysql_dao.create(request)
        kube_cluster = config.get_kube_cluster(request.cloud_env)
        dcm.start_spark_history_server(request, kube_cluster, namespace)
        return Response.success_response("Spark History Server Created Successfully", {"server_id": resp})
    except Exception as e:
        request.status = SparkHSStatus.FAILED
        mysql_dao.update(request)
        logger.exception(f"Error creating spark history server: {request}: {e}")
        return Response.internal_server_error_response("Error creating spark history server", e.__str__())


async def stop(
    spark_history_server_id: str, mysql_dao: SparkHistoryServerDAO, dcm: DarwinClusterManager, config: Config
) -> JSONResponse:
    try:
        logger.info(f"Stopping spark history server by ID: {spark_history_server_id}")
        shs_data = mysql_dao.get_by_id(spark_history_server_id)
        if not shs_data:
            logger.error(f"Spark History Server Not Found: {spark_history_server_id}")
            return Response.not_found_error_response(
                "Spark History Server Not Found", {"server_id": spark_history_server_id}
            )
        if shs_data.status not in [SparkHSStatus.ACTIVE, SparkHSStatus.CREATED]:
            logger.error(f"Cannot stop inactive spark history server: {shs_data}")
            return Response.forbidden_error_response(
                "Cannot stop inactive spark history server", {"server_id": spark_history_server_id}
            )

        kube_cluster = config.get_kube_cluster(shs_data.cloud_env)
        namespace = config.default_namespace
        dcm.stop_spark_history_server(spark_history_server_id, kube_cluster, namespace)
        mysql_dao.update_status(spark_history_server_id, SparkHSStatus.INACTIVE)
        return Response.success_response(
            "Spark History Server Stopped Successfully", {"server_id": spark_history_server_id}
        )
    except Exception as e:
        logger.exception(f"Error stopping spark history server by ID: {spark_history_server_id}: {e}")
        return Response.internal_server_error_response("Error stopping spark history server by ID", e.__str__())


async def delete(spark_history_server_id: str, mysql_dao: SparkHistoryServerDAO) -> JSONResponse:
    try:
        logger.info(f"Deleting spark history server by ID: {spark_history_server_id}")
        shs = mysql_dao.get_by_id(spark_history_server_id)
        if not shs:
            logger.error(f"Spark History Server Not Found: {spark_history_server_id}")
            return Response.not_found_error_response(
                "Spark History Server Not Found", {"server_id": spark_history_server_id}
            )
        if shs.status in [SparkHSStatus.CREATED, SparkHSStatus.ACTIVE]:
            logger.error(f"Cannot delete active spark history server: {shs}")
            return Response.forbidden_error_response(
                "Cannot delete active spark history server", {"server_id": spark_history_server_id}
            )
        mysql_dao.delete_by_id(spark_history_server_id)
        logger.info(f"Spark History Server Deleted Successfully: {spark_history_server_id}")
        return Response.success_response(
            "Spark History Server Deleted Successfully", {"server_id": spark_history_server_id}
        )
    except Exception as e:
        logger.exception(f"Error deleting spark history server by ID: {spark_history_server_id}: {e}")
        return Response.internal_server_error_response("Error deleting spark history server by ID", e.__str__())
