from typing import Optional

from loguru import logger

from compute_app_layer.models.spark_history_server import (
    SparkHistoryServer,
    SparkHSStatus,
)
from compute_core.constant.constants import CLOUD_ENV_CONFIG_KEY_SHS
from compute_core.dao.mysql_dao import MySQLDao
from compute_core.dao.queries.sql_queries import (
    GET_ALL_SPARK_HISTORY_SERVERS,
    GET_SPARK_HISTORY_SERVER_BY_ID,
    INSERT_SPARK_HISTORY_SERVER,
    DELETE_SPARK_HISTORY_SERVER,
    GET_ALL_ACTIVE_SPARK_HISTORY_SERVERS,
    GET_SPARK_HISTORY_SERVER_BY_RESOURCE,
    UPDATE_SPARK_HISTORY_SERVER_STATUS,
    UPDATE_SPARK_HISTORY_SERVER,
    GET_CLUSTER_CONFIG,
)


class SparkHistoryServerDAO:
    def __init__(self, env: str = None):
        self._mysql_dao = MySQLDao(env)

    def get_all(self, limit: int, offset: int) -> list[SparkHistoryServer]:
        query = GET_ALL_SPARK_HISTORY_SERVERS
        data = {"limit": limit, "offset": offset}
        resp = self._mysql_dao.read(query, data)
        logger.debug(f"Get All Spark History Servers from {offset} to {limit+offset} DB Response: {resp}")
        resp = [SparkHistoryServer(**r) for r in resp]
        return resp

    def get_all_active(self, limit: int, offset: int) -> list[SparkHistoryServer]:
        query = GET_ALL_ACTIVE_SPARK_HISTORY_SERVERS
        data = {"limit": limit, "offset": offset}
        resp = self._mysql_dao.read(query, data)
        logger.debug(f"Get All Active Spark History Servers from {offset} to {limit+offset} DB Response: {resp}")
        resp = [SparkHistoryServer(**r) for r in resp]
        return resp

    def get_by_id(self, spark_history_server_id: str) -> Optional[SparkHistoryServer]:
        query = GET_SPARK_HISTORY_SERVER_BY_ID
        data = {"id": spark_history_server_id}
        resp = self._mysql_dao.read(query, data)
        logger.debug(f"Get Spark History Server By Id {spark_history_server_id}: DB Response: {resp}")
        resp = [SparkHistoryServer(**r) for r in resp]
        return resp[0] if resp else None

    def get_by_resource(self, resource: str) -> Optional[SparkHistoryServer]:
        query = GET_SPARK_HISTORY_SERVER_BY_RESOURCE
        data = {"resource": resource}
        resp = self._mysql_dao.read(query, data)
        logger.debug(f"Get Spark History Server By Resource {resource}: DB Response: {resp}")
        resp = [SparkHistoryServer(**r) for r in resp]
        return resp[0] if resp else None

    def create(self, req: SparkHistoryServer) -> str:
        query = INSERT_SPARK_HISTORY_SERVER
        data = req.model_dump()
        logger.debug(f"Create Spark History Server in DB Request: {data}")
        resp, _ = self._mysql_dao.create(query, data)
        logger.debug(f"Create Spark History Server {req.id}: DB Response: {resp}")
        return req.id

    def update(self, req: SparkHistoryServer) -> str:
        query = UPDATE_SPARK_HISTORY_SERVER
        data = req.model_dump()
        logger.debug(f"Update Spark History Server in DB Request: {data}")
        resp, _ = self._mysql_dao.update(query, data)
        logger.debug(f"Update Spark History Server {req.id}: DB Response: {resp}")
        return req.id

    def update_status(self, spark_history_server_id: str, status: SparkHSStatus) -> int:
        query = UPDATE_SPARK_HISTORY_SERVER_STATUS
        data = {"id": spark_history_server_id, "status": status.value}
        logger.debug(f"Update Spark History Server Status DB Request: {data}")
        resp, _ = self._mysql_dao.update(query, data)
        logger.debug(f"Update Spark History Server Status {spark_history_server_id}: DB Response: {resp}")
        return resp

    def delete_by_id(self, spark_history_server_id: str) -> int:
        query = DELETE_SPARK_HISTORY_SERVER
        data = {"id": spark_history_server_id}
        logger.debug(f"Delete Spark History Server DB Request: {data}")
        resp, _ = self._mysql_dao.delete(query, data)
        logger.debug(f"Delete Spark History Server for ID {spark_history_server_id}: DB Response: {resp}")
        return resp

    def get_default_cloud_env(self):
        query = GET_CLUSTER_CONFIG
        data = {"key": CLOUD_ENV_CONFIG_KEY_SHS}
        resp = self._mysql_dao.read(query, data)
        logger.debug(f"Get Default Cloud Env DB Response: {resp}")
        return resp[0]["value"]
