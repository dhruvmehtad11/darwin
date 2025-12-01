from typing import Callable

from typeguard import typechecked

from workspace_core.constants.config import Config
from workspace_core.utils.logging_util import get_logger
from workspace_core.utils.mysql_connection import Connection, ConnectionPool

logger = get_logger(__name__)


class CustomQuery:
    def __init__(self, mysql_connection: Connection):
        self.mysql_connection = mysql_connection

    def __enter__(self):
        return self.mysql_connection

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type is None:
            self.mysql_connection.commit()
            if self.mysql_connection.connector.is_connected():
                self.mysql_connection.close()
        else:
            self.mysql_connection.rollback()
            logger.error(f"Error in Executing Query: {exc_type} {exc_val} {exc_tb}")
            if self.mysql_connection.connector.is_connected():
                self.mysql_connection.close()
            raise exc_type(exc_val)


@typechecked
class MySQLDao:
    """
    For executing read/write/update/delete queries in MySQL using connection from connection pool
    """

    def __init__(self, env: str):
        config = Config(env).db_config()
        self.connection_pool = ConnectionPool(config).get_connection_pool()

    def _get_connection(self):
        """
        Gets a connection from connection pool
        """
        return Connection(self.connection_pool.get_connection())

    def healthcheck(self):
        """
        Healthcheck of MySQL DB connection
        :return: True/False
        """
        mysql_connection = self._get_connection()
        try:
            resp = mysql_connection.connector.is_connected()
            return resp
        finally:
            mysql_connection.close()

    def create(self, query: str, data: dict, func: Callable = lambda: None):
        """
        For writing data to MySQL DB
        :param query: SQL query
        :param data: Data to be written
        :param func: Function to be executed in transaction
        :return: id of last row executed, func_resp
        """
        with CustomQuery(self._get_connection()) as mysql_connection:
            mysql_connection.execute_query(query, data)
            func_resp = func()
            return mysql_connection.cursor.lastrowid, func_resp

    def read(self, query: str, data: dict = None):
        """
        For reading data from MySQL DB
        :param query: SQL query
        :param data: Data to be written
        :return: result returned after executing query
        """
        data = data if data else {}
        with CustomQuery(self._get_connection()) as mysql_connection:
            mysql_connection.execute_query(query, data)
            return mysql_connection.cursor.fetchall()

    def read_one(self, query: str, data: dict = None):
        """
        For reading data from MySQL DB
        :param query: SQL query
        :param data: Data to be written
        :return: result returned after executing query
        """
        data = data if data else {}
        with CustomQuery(self._get_connection()) as mysql_connection:
            mysql_connection.execute_query(query, data)
            return mysql_connection.cursor.fetchone()

    def update(self, query: str, data: dict, func: Callable = lambda: None):
        """
        For updating data from MySQL DB
        :param query: SQL query
        :param data: Data to be written
        :param func: Function to be executed in transaction
        :return: total number of rows in which changes were made, func_resp
        """
        with CustomQuery(self._get_connection()) as mysql_connection:
            mysql_connection.execute_query(query, data)
            func_resp = func()
            return mysql_connection.cursor.rowcount, func_resp

    def delete(self, query: str, data: dict, func: Callable = lambda: None):
        """
        For deleting data from MySQL DB
        :param query: SQL query
        :param data: Data to be written
        :param func: Function to be executed in transaction
        :return: total number of deleted rows, func_resp
        """
        with CustomQuery(self._get_connection()) as mysql_connection:
            mysql_connection.execute_query(query, data)
            func_resp = func()
            return mysql_connection.cursor.rowcount, func_resp
