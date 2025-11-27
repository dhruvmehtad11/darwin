from typing import Optional

from loguru import logger

from compute_core.dao.mysql_dao import MySQLDao, CustomTransaction
from compute_core.dao.queries.sql_queries import (
    INSERT_REMOTE_COMMAND,
    INSERT_PODS_COMMAND_EXECUTION,
    START_REMOTE_COMMAND_EXECUTION,
    UPDATE_REMOTE_COMMAND_EXECUTION_STATUS,
    GET_REMOTE_COMMAND_EXECUTION_STATUS,
    DELETE_REMOTE_COMMAND,
    GET_ALL_REMOTE_COMMANDS_OF_CLUSTER,
    UPDATE_CLUSTER_COMMAND_EXECUTION_STATUS_TO_RUNNING,
    GET_ERROR_DETAILS_FOR_REMOTE_COMMAND,
    UPDATE_RUNNING_COMMANDS_STATUS_TO_CREATED,
)
from compute_core.dto.exceptions import ExecutionNotFoundError
from compute_core.dto.remote_command_dto import PodCommandExecutionStatusDto, RemoteCommandDto


class RemoteCommandDao(MySQLDao):
    """
    For executing remote command queries in MySQL using connection from connection pool
    """

    def __init__(self, env: str = None):
        super().__init__(env)

    def add_remote_commands(self, remote_command_dtos: list[RemoteCommandDto], cluster_id: str) -> None:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = INSERT_REMOTE_COMMAND
            data = [
                {
                    "execution_id": remote_command_dto.execution_id,
                    "cluster_id": cluster_id,
                    "command": remote_command_dto.command,
                    "target": remote_command_dto.target.value,
                    "status": remote_command_dto.status.value,
                    "timeout": remote_command_dto.timeout,
                }
                for remote_command_dto in remote_command_dtos
            ]
            mysql_connection.execute_many(query, data)
            logger.debug(f"Added remote commands to the database")

    def start_remote_command_execution(self, execution_id: str) -> None:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = START_REMOTE_COMMAND_EXECUTION
            data = {"execution_id": execution_id}
            mysql_connection.execute_query(query, data)

    def remove_remote_command(self, execution_ids: list[str]) -> None:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = DELETE_REMOTE_COMMAND
            data = [{"execution_id": execution_id} for execution_id in execution_ids]
            mysql_connection.execute_many(query, data)

    def update_remote_command_execution_status(self, execution_id: str, status: str) -> None:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = UPDATE_REMOTE_COMMAND_EXECUTION_STATUS
            data = {"execution_id": execution_id, "status": status}
            mysql_connection.execute_query(query, data)

    def get_all_remote_command_execution_status(self, cluster_id: str) -> Optional[list[dict]]:
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            query = GET_ALL_REMOTE_COMMANDS_OF_CLUSTER
            data = {"cluster_id": cluster_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.fetchall()
            return resp

    def get_remote_command_execution_status(self, execution_id: str) -> str:
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            query = GET_REMOTE_COMMAND_EXECUTION_STATUS
            data = {"execution_id": execution_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.fetchone()
            if not resp:
                logger.debug(f"Remote command execution status not found for execution id: {execution_id}")
                raise ExecutionNotFoundError(execution_id)
            return resp["status"]

    def insert_pod_command_execution_status(
        self, pod_command_execution_status: PodCommandExecutionStatusDto
    ) -> Optional[int]:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = INSERT_PODS_COMMAND_EXECUTION
            data = {
                "cluster_run_id": pod_command_execution_status.cluster_run_id,
                "execution_id": pod_command_execution_status.execution_id,
                "pod_name": pod_command_execution_status.pod_name,
                "status": pod_command_execution_status.status,
            }
            mysql_connection.execute_query(query, data)
            return mysql_connection.cursor.lastrowid

    def update_remote_commands_status_for_cluster_to_running(self, cluster_id: str) -> Optional[int]:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = UPDATE_CLUSTER_COMMAND_EXECUTION_STATUS_TO_RUNNING
            data = {"cluster_id": cluster_id}
            mysql_connection.execute_query(query, data)
            return mysql_connection.cursor.rowcount

    def update_running_commands_status_to_created(self, cluster_id: str) -> Optional[int]:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = UPDATE_RUNNING_COMMANDS_STATUS_TO_CREATED
            data = {"cluster_id": cluster_id}
            mysql_connection.execute_query(query, data)
            return mysql_connection.cursor.rowcount

    def get_error_details_for_remote_command(self, execution_id: str) -> Optional[str]:
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            query = GET_ERROR_DETAILS_FOR_REMOTE_COMMAND
            data = {"execution_id": execution_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.fetchone()
            return resp
