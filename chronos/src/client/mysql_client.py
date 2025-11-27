import os

from tortoise import Tortoise

from src.client.mysql_config import MysqlConfig
from src.config import ConfigReader
from src.config.ApplicationConfig import ApplicationConfig

app_config = ApplicationConfig.get_config()

from loguru import logger
from tortoise.contrib.fastapi import register_tortoise
from fastapi import FastAPI



class CustomReadReplicaRouter:
    def db_for_read(self, model, **hints):
        return "reader"

    def db_for_write(self, model, **hints):
        return "default"


class MysqlClient:

    def __init__(self):
        logger.info("initiating db")
        file_dir = app_config.app_dir + "/resources/config/mysql/"
        file_path = file_dir + "connection-" + app_config.config_file_suffix + ".conf"
        default_file_path = file_dir + "/connection-default.conf"
        config: MysqlConfig = MysqlConfig(
            **ConfigReader.read_config(file_path if os.path.exists(file_path) else default_file_path))
        self.config = config
        self.modules = {'models': ['src.models.models']}

    def init_db(self, app: FastAPI):
        register_tortoise(
            app,
            config={
                "connections": {
                    "default": {
                        "engine": "tortoise.backends.mysql",
                        "credentials": {
                            "host": self.config.masterHost,
                            "port": 3306,
                            "user": self.config.username,
                            "password": self.config.password,
                            "database": self.config.database
                        }
                    },
                    "reader": {
                        "engine": "tortoise.backends.mysql",
                        "credentials": {
                            "host": self.config.slaveHost,
                            "port": 3306,
                            "user": self.config.username,
                            "password": self.config.password,
                            "database": self.config.database
                        }
                    },
                },
                "apps": {
                    "models": {
                        "models": ['src.models.models'],
                        "default_connection": "default"
                    }
                },
                "routers": ["src.client.mysql_client.CustomReadReplicaRouter"]
            },
            modules=self.modules,
            generate_schemas=False,
            add_exception_handlers=True,
        )

    def init_db_consumer(self, app: FastAPI):
        register_tortoise(
            app,
            db_url=f'mysql://{self.config.username}:{self.config.password}@{self.config.masterHost}/{self.config.database}',
            modules=self.modules,
            generate_schemas=False,
            add_exception_handlers=True,
        )

    async def init_tortoise(self):
        await Tortoise.generate_schemas()

    async def close_tortoise(self):
        await Tortoise.close_connections()
