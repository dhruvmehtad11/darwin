import json
from typing import Optional

from serve_core.dao.mysql_dao import MySQLDao
from serve_core.dao.queries.sql_queries import (
    LIST_ALL_TASKS,
    GET_BUILD_STATUS,
    GET_LOGS,
    CREATE_RUNTIME,
    UPDATE_IMAGE_BUILD_STATUS,
    GET_WAITING_TASK,
    GET_IMAGE_TAG,
    GET_APP_NAME,
    GET_BUILD_PARAMS,
)
from serve_core.utils.mysql_connection import Connection


class CustomTransaction:
    def __init__(self, mysql_connection):
        self.mysql_connection = mysql_connection

    def __enter__(self) -> Connection:
        self.mysql_connection.start_transaction()
        return self.mysql_connection

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type is None:
            self.mysql_connection.commit()
        else:
            self.mysql_connection.rollback()
        if self.mysql_connection.connector.is_connected():
            self.mysql_connection.close()


class RuntimeDao:
    def __init__(self, env: str):
        self._mysql_dao = MySQLDao(env)

    def healthcheck(self):
        return self._mysql_dao.healthcheck()

    def get_task_status(self, task_id: str):
        sql_query = GET_BUILD_STATUS
        sql_data = {"task_id": task_id}
        result = self._mysql_dao.read(sql_query, sql_data)
        if not result:
            raise Exception(f"Task does not exist")
        return result[0]["status"]

    def create_task(self, task_id: str, image_tag: str, app_name: str, logs_url: str, build_params: dict, status: str):
        sql_query = CREATE_RUNTIME
        sql_data = {
            "task_id": task_id,
            "app_name": app_name,
            "image_tag": image_tag,
            "logs_url": logs_url,
            "build_params": json.dumps(build_params),
            "status": status,
        }

        result = self._mysql_dao.create(sql_query, sql_data)
        return result

    def get_logs_url(self, task_id: str):
        sql_query = GET_LOGS
        sql_data = {"task_id": task_id}
        result = self._mysql_dao.read(sql_query, sql_data)
        if not result:
            raise Exception(f"Task does not exist")
        return result[0]["logs_url"]

    def update_status(self, task_id: str, status: str):
        sql_query = UPDATE_IMAGE_BUILD_STATUS
        sql_data = {"task_id": task_id, "status": status}
        result = self._mysql_dao.update(sql_query, sql_data)
        return result

    def get_all_tasks(self):
        sql_query = LIST_ALL_TASKS
        result = self._mysql_dao.read(sql_query)
        return result

    def get_waiting_task_and_update_status(self):
        with CustomTransaction(self._mysql_dao.get_connection()) as mysql_connection:
            sql_query = GET_WAITING_TASK
            mysql_connection.execute_query(sql_query)
            result = mysql_connection.cursor.fetchone()
            if not result:
                return None
            task_id = result["task_id"]

            sql_query = UPDATE_IMAGE_BUILD_STATUS
            sql_data = {"task_id": task_id, "status": "running"}
            result = mysql_connection.execute_query(sql_query, sql_data)
            mysql_connection.commit()
            return task_id

    def get_image_tag(self, task_id: str):
        sql_query = GET_IMAGE_TAG
        sql_data = {"task_id": task_id}
        result = self._mysql_dao.read(sql_query, sql_data)
        if not result:
            raise Exception(f"Task does not exist")
        return result[0]["image_tag"]

    def get_app_name(self, task_id: str):
        sql_query = GET_APP_NAME
        sql_data = {"task_id": task_id}
        result = self._mysql_dao.read(sql_query, sql_data)
        if not result:
            raise Exception(f"Task does not exist")
        return result[0]["app_name"]

    def get_build_params(self, task_id: str):
        sql_query = GET_BUILD_PARAMS
        sql_data = {"task_id": task_id}
        result = self._mysql_dao.read(sql_query, sql_data)
        if not result:
            raise Exception(f"Task does not exist")
        return result[0]["build_params"]
