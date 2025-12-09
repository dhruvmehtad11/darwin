from workflow_core.constants.configs import Config
from workflow_core.utils.logging_util import LoggingUtil
from tortoise import Tortoise
from tortoise.contrib.fastapi import register_tortoise
from aerich import Command
from fastapi import FastAPI
import aiomysql

logger = LoggingUtil().get_logger()


class darwin_worflow_conn:
    def __init__(self, env: str):
        self.conn = Config(env).get_workflow_conn
        self.workflow_config = Config(env).get_workflow_config
        self.pool = None
        self.modules = {'models': ["workflow_model.darwin_workflow"]}

    async def ensure_database_exists(self):
        host = self.workflow_config.get('host', 'localhost')
        port = self.workflow_config.get('port', 3306)
        user = self.workflow_config.get('username', 'root')
        password = self.workflow_config.get('password', 'root')
        database = self.workflow_config['database']
        # Connect to MySQL server (not to a specific database)
        conn = await aiomysql.connect(host=host, port=int(port), user=user, password=password)
        async with conn.cursor() as cur:
            await cur.execute(f"CREATE DATABASE IF NOT EXISTS `{database}`;")
        await conn.ensure_closed()
        logger.info(f"✅ Ensured database '{database}' exists.")

    def init_db(self, app: FastAPI):
        register_tortoise(
            app,
            db_url=self.conn,
            modules={k: tuple(v) for k, v in self.modules.items()},
            generate_schemas=False,  # Disable sync schema generation - handled in async startup
            add_exception_handlers=True,
        )
        logger.info("✅ Register tortoise completed")

    async def init_tortoise(self):
        """
        Initializes Tortoise ORM and applies Aerich migrations with proper error handling and logging.
        """
        await self.ensure_database_exists()
        TORTOISE_ORM = {
            "connections": {
                "default": {
                    "engine": "tortoise.backends.mysql",
                    "credentials": {
                        "database": self.workflow_config.get('database', 'darwin_workflow'),
                        "host": self.workflow_config.get('host', 'localhost'),
                        "user": self.workflow_config.get('username', 'root'),
                        "password": self.workflow_config.get('password', 'root'),
                        "port": self.workflow_config.get('port', 3306),
                    },
                }
            },
            "apps": {
                "models": {
                    "models": ["workflow_model.darwin_workflow", "workflow_model.v3.darwin_workflow", "aerich.models"],  # Ensure both models are registered
                    "default_connection": "default",
                }
            },
        }

        command = Command(tortoise_config=TORTOISE_ORM)

        # Initialize Tortoise ORM
        try:
            logger.info("✅ Tortoise initialization started.")
            await Tortoise.init(TORTOISE_ORM)
            logger.info("✅ Tortoise ORM initialized successfully.")
        except Exception as e:
            logger.error(f"❌ Failed to initialize Tortoise ORM: {e}", exc_info=True)
            return

        # Initialize Aerich
        try:
            await command.init()
            logger.info("✅ Aerich initialization completed.")
        except Exception as e:
            logger.error(f"❌ Aerich initialization failed: {e}", exc_info=True)
            return

        # Create database tables if missing
        try:
            await command.init_db(safe=True)
            logger.info("✅ `init_db` completed successfully.")
        except Exception as e:
            logger.error(f"❌ `init_db` failed: {e}", exc_info=True)

        # Run migrations
        try:
            await command.migrate()
            logger.info("✅ Migrations generated successfully.")
        except Exception as e:
            logger.error(f"❌ Migrations failed: {e}", exc_info=True)

        # Apply migrations
        try:
            await command.upgrade(run_in_transaction=True)
            logger.info("✅ Database upgrade completed successfully.")
        except Exception as e:
            logger.error(f"❌ Database upgrade failed: {e}", exc_info=True)

    async def close_tortoise(self):
        await Tortoise.close_connections()
