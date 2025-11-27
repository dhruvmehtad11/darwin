import datetime
from typing import Optional
from loguru import logger

from compute_core.dao.mysql_dao import MySQLDao, CustomTransaction
from compute_core.dto.remote_command_dto import PodCommandExecutionStatusDto
from compute_script.dao.queries.sql_queries import (
    GET_CLUSTER,
    UPDATE_CLUSTER_LAST_PICKED_AT,
    UPDATE_CLUSTER_LAST_USED_AT,
    UPDATE_CLUSTER_LAST_UPDATED_AT,
    GET_CLUSTER_ACTION_TIME,
    GET_CLUSTER_ACTION_LATEST_AND_ENDING_TIME,
    GET_RUNNING_REMOTE_COMMANDS,
    GET_PODS_COMMAND_EXECUTION_STATUS,
    UPDATE_REMOTE_COMMAND_EXECUTION_STATUS,
    UPDATE_REMOTE_COMMANDS_LAST_PICKED_AT,
    GET_ACTIVE_CLUSTER_RUN_ID_WITH_CLUSTER_ID,
)
from compute_script.dto.cluster_metadata import ClusterMetadata
from compute_script.dto.remote_command_dto import RunningRemoteCommandDto


class ScriptMySQLDao(MySQLDao):
    """
    For executing compute script queries in MySQL using connection from connection pool
    """

    def __init__(self, env: str = None):
        super().__init__(env)

    def get_unpicked_cluster(self):
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            mysql_connection.execute_query(GET_CLUSTER)
            resp = mysql_connection.cursor.fetchone()
            logger.debug(f"Picked cluster: {resp}")
            if not resp:
                return None
            mysql_connection.execute_query(UPDATE_CLUSTER_LAST_PICKED_AT, {"cluster_id": resp["cluster_id"]})
            mysql_connection.commit()
            cluster = ClusterMetadata.from_dict(resp)
            return cluster

    def update_last_used_at(self, cluster_id):
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            mysql_connection.execute_query(UPDATE_CLUSTER_LAST_USED_AT, {"cluster_id": cluster_id})

    def update_cluster_last_updated_at(self, cluster_id: str):
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            mysql_connection.execute_query(UPDATE_CLUSTER_LAST_UPDATED_AT, {"cluster_id": cluster_id})

    def get_cluster_action_time(self, run_id: str, cluster_action: str) -> Optional[datetime.datetime]:
        """
        Gets latest time of given cluster_action for given cluster run_id
        """
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            mysql_connection.execute_query(
                GET_CLUSTER_ACTION_TIME,
                {"run_id": run_id, "cluster_action": cluster_action},
            )
            resp = mysql_connection.cursor.fetchone()
            if not resp:
                return None
            return resp["updated_at"]

    def get_cluster_action_ending_time(self, run_id: str, cluster_action: str) -> Optional[datetime.datetime]:
        """
        Gets ending time of given cluster_action for given cluster run_id.
        Ending time of a cluster action is the time of action just after given
        cluster_action if next cluster action exists else just the time of given cluster action
        """
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            mysql_connection.execute_query(
                GET_CLUSTER_ACTION_LATEST_AND_ENDING_TIME,
                {"run_id": run_id, "cluster_action": cluster_action},
            )
            resp = mysql_connection.cursor.fetchone()

            times = [value for value in resp.values() if value is not None]

            latest_time = max(times) if times else None

            return latest_time

    def get_running_remote_commands(self) -> Optional[list[RunningRemoteCommandDto]]:
        """
        Get the remote commands which are currently running
        """
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            mysql_connection.execute_query(GET_RUNNING_REMOTE_COMMANDS)
            resp = mysql_connection.cursor.fetchall()
            if not resp:
                return None
            mysql_connection.execute_many(
                UPDATE_REMOTE_COMMANDS_LAST_PICKED_AT,
                [{"execution_id": rc["execution_id"], "cluster_id": rc["cluster_id"]} for rc in resp],
            )
            return [RunningRemoteCommandDto.from_dict(rc) for rc in resp]

    def get_pods_command_execution_status(self, run_id: str, execution_id: str) -> list[PodCommandExecutionStatusDto]:
        """
        Get the pods command execution status with run_id and execution_id
        """
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            mysql_connection.execute_query(
                GET_PODS_COMMAND_EXECUTION_STATUS, {"cluster_run_id": run_id, "execution_id": execution_id}
            )
            resp = mysql_connection.cursor.fetchall()
            return [PodCommandExecutionStatusDto.from_dict(pod_status) for pod_status in resp]

    def get_active_cluster_run_id(self, cluster_id: str):
        """
        Get the cluster run id using cluster id
        """
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            mysql_connection.execute_query(GET_ACTIVE_CLUSTER_RUN_ID_WITH_CLUSTER_ID, {"cluster_id": cluster_id})
            resp = mysql_connection.cursor.fetchone()
            return resp["active_cluster_runid"]

    def update_remote_command_execution_status(
        self, execution_id: str, status: str, error_logs_path: str = None, error_code: str = None
    ):
        """
        Update the status of remote command execution
        """
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            mysql_connection.execute_query(
                UPDATE_REMOTE_COMMAND_EXECUTION_STATUS,
                {
                    "execution_id": execution_id,
                    "status": status,
                    "error_logs_path": error_logs_path,
                    "error_code": error_code,
                },
            )
            resp = mysql_connection.commit()
            return resp
