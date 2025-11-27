from opentelemetry.instrumentation.mysql import MySQLInstrumentor
from mysql.connector import pooling
from mysql.connector.connection import MySQLConnection


class ConnectionPool:
    """
    Creates a connection pool with the config provided
    """

    def __init__(self, config):
        self.connection_pool = pooling.MySQLConnectionPool(
            pool_name=config["database"] + "_pool", pool_size=9, **config
        )

    def get_connection_pool(self):
        """
        Returns a connection pool
        """
        return self.connection_pool


class Connection:
    """
    Base class for the MySQL connection
    """

    def __init__(self, connector: MySQLConnection):
        self.connector = MySQLInstrumentor().instrument_connection(connector)
        self.cursor = self.connector.cursor(dictionary=True)

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
