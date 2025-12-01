import os

from loguru import logger
from tortoise import Tortoise

from ml_serve_core.client.mysql_config import MysqlConfig
from tortoise.contrib.fastapi import register_tortoise
from fastapi import FastAPI


class MysqlClient:
    def __init__(self):
        logger.info("Initializing MySQL Client")
        # Read database configuration from MYSQL_* environment variables
        master_host = os.getenv('MYSQL_HOST', 'localhost')
        slave_host = os.getenv('MYSQL_SLAVE_HOST') or master_host
        database = os.getenv('MYSQL_DATABASE', 'darwin_ml_serve')
        username = os.getenv('MYSQL_USERNAME', 'root')
        password = os.getenv('MYSQL_PASSWORD', 'password')

        self.config = MysqlConfig(
            masterHost=master_host,
            slaveHost=slave_host,
            database=database,
            username=username,
            password=password
        )
        self.modules = {'models': ["ml_serve_model"]}

    def init_db(self, app: FastAPI):
        register_tortoise(
            app,
            db_url=f'mysql://{self.config.username}:{self.config.password}@{self.config.masterHost}/{self.config.database}',
            modules=self.modules,
            generate_schemas=True,
            add_exception_handlers=True,
        )

    async def init_tortoise(self):
        await Tortoise.generate_schemas()

    async def close_tortoise(self):
        await Tortoise.close_connections()
