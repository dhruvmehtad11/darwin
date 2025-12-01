from loguru import logger
from opentelemetry.instrumentation.mysql import MySQLInstrumentor
from mysql.connector import pooling
from mysql.connector.connection import MySQLConnection
from typing import Callable


class ConnectionPool:
    """
    Creates separate connection pools for read and write operations.
    """

    @staticmethod
    def create_connection_pool(
        pool_name: str, pool_size: int, host: str, user: str, password: str, database: str, port: str
    ):
        pool = pooling.MySQLConnectionPool(
            pool_name=pool_name,
            pool_size=pool_size,
            host=host,
            user=user,
            password=password,
            database=database,
            port=port,
        )
        logger.info(f"Pool '{pool_name}' created successfully.")
        return pool

    def __init__(self, config):
        """
        Initializes the ConnectionPool with read and write configurations.
        """
        self.write_pool = ConnectionPool.create_connection_pool(
            "write_pool",
            config["pool_size"],
            config["write_host"],
            config["user"],
            config["password"],
            config["database"],
            config["port"],
        )

        self.read_pool = ConnectionPool.create_connection_pool(
            "read_pool",
            config["pool_size"],
            config["read_host"],
            config["user"],
            config["password"],
            config["database"],
            config["port"],
        )


class Connection:
    """
    Base class for the MySQL connection
    """

    def __init__(self, connector: MySQLConnection):
        self.connector = MySQLInstrumentor().instrument_connection(connector)
        self.cursor = self.connector.cursor(dictionary=True)

    def read(self, query: str, data: dict):
        """
        For reading data from MySQL DB
        :param query: SQL Query
        :param data: Values to be fed inside SQL Query.
        :return: result returned after executing query
        """
        self.cursor.execute(query, data)
        result = self.cursor.fetchall()
        return result

    def write(self, query: str, data: dict, func: Callable):
        """
        For writing transaction to MySQL DB
        :param query: SQL Query
        :param data: Values to be fed inside SQL Query.
        :param func: function to be executed during the transaction.
        :return: id of last row executed and total number of rows in which changes were made and func response
        """
        try:
            self.cursor.execute(query, data)
            func_resp = func()
            self.commit()
            return self.cursor.lastrowid, self.cursor.rowcount, func_resp
        except Exception as e:
            self.rollback()
            raise e

    def commit(self):
        """
        Commits the transaction
        """
        self.connector.commit()

    def rollback(self):
        """
        Rolls back the transaction
        """
        self.connector.rollback()

    def close(self):
        """
        Closes MySQL cursor & connector
        """
        self.cursor.close()
        self.connector.close()

    def start_transaction(self):
        """
        Starts a transaction
        """
        self.connector.start_transaction()

    def execute_query(self, query: str, data: dict = None):
        """
        Executes a query
        """
        self.cursor.execute(query, data)

    def execute_many(self, query: str, data: list[dict]):
        """
        Executes a query multiple times
        """
        self.cursor.executemany(query, data)
