from typing import Callable, Optional

from typeguard import typechecked

from serve_core.constant.serve_constants import Config
from serve_core.utils.mysql_connection import Connection, ConnectionPool

connection_pool = None


@typechecked
class MySQLDao:
    """
    For executing read/write/update/delete queries in MySQL using connection from connection pool
    """

    def __init__(self, env: str):
        config = Config(env).db_config()
        global connection_pool
        connection_pool = connection_pool if connection_pool else ConnectionPool(config).get_connection_pool()
        self.connection_pool = connection_pool

    def get_connection(self) -> Connection:
        """
        Gets a connection from connection pool
        """
        return Connection(self.connection_pool.get_connection())

    def healthcheck(self):
        """
        Healthcheck of MySQL DB connection
        :return: True/False
        """
        mysql_connection = self.get_connection()
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
        mysql_connection = self.get_connection()
        try:
            created, _, func_resp = mysql_connection.write(query, data, func)
            return created, func_resp
        finally:
            if mysql_connection.connector.is_connected():
                mysql_connection.close()

    def read(self, query: str, data: Optional[dict] = None):
        """
        For reading data from MySQL DB
        :param query: SQL query
        :param data: Data to be written
        :return: result returned after executing query
        """
        data = data if data else {}
        mysql_connection = self.get_connection()
        try:
            read_res = mysql_connection.read(query, data)
            return read_res
        finally:
            if mysql_connection.connector.is_connected():
                mysql_connection.close()

    def update(self, query: str, data: dict, func: Callable = lambda: None):
        """
        For updating data from MySQL DB
        :param query: SQL query
        :param data: Data to be written
        :param func: Function to be executed in transaction
        :return: total number of rows in which changes were made, func_resp
        """
        mysql_connection = self.get_connection()
        try:
            _, updated, func_resp = mysql_connection.write(query, data, func)
            return updated, func_resp
        finally:
            if mysql_connection.connector.is_connected():
                mysql_connection.close()

    def delete(self, query: str, data: dict, func: Callable = lambda: None):
        """
        For deleting data from MySQL DB
        :param query: SQL query
        :param data: Data to be written
        :param func: Function to be executed in transaction
        :return: total number of deleted rows, func_resp
        """
        mysql_connection = self.get_connection()
        try:
            _, deleted, func_resp = mysql_connection.write(query, data, func)
            return deleted, func_resp
        finally:
            if mysql_connection.connector.is_connected():
                mysql_connection.close()
