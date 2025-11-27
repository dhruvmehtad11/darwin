import os
import aiomysql
from loguru import logger
from tortoise import Tortoise
from tortoise.contrib.fastapi import register_tortoise
from fastapi import FastAPI


class MysqlClient:
    """
    MySQL database client using Tortoise ORM
    """
    
    def __init__(self):
        logger.info("Initializing MySQL Client")
        
        # Get database configuration from environment variables
        self.host = os.getenv("MYSQL_HOST", "localhost")
        self.port = int(os.getenv("MYSQL_PORT", "3306"))
        self.username = os.getenv("MYSQL_USERNAME", "root")
        self.password = os.getenv("MYSQL_PASSWORD", "")
        self.database = os.getenv("MYSQL_DATABASE", "mlp_serve")
        self.env = os.getenv("ENV", "local")
        
        # Tortoise ORM models module - specify exact module path for model discovery
        self.modules = {'models': ["serve_model.image_builder"]}
        
        # Construct database URL - handle empty password correctly
        if self.password:
            self.db_url = f'mysql://{self.username}:{self.password}@{self.host}:{self.port}/{self.database}'
        else:
            # For empty password, omit the colon entirely
            self.db_url = f'mysql://{self.username}@{self.host}:{self.port}/{self.database}'
        
        logger.info(f"Database URL: mysql://{self.username}:***@{self.host}:{self.port}/{self.database}")

    async def ensure_database_exists(self):
        """
        Create database if it doesn't exist (development only)
        """
        if self.env not in ["local", "dev"]:
            logger.info("Production environment - skipping database creation")
            return
            
        try:
            # Connect without specifying database
            connect_params = {
                "host": self.host,
                "port": self.port,
                "user": self.username,
            }
            # Only add password if it's not empty
            if self.password:
                connect_params["password"] = self.password
            
            conn = await aiomysql.connect(**connect_params)
            async with conn.cursor() as cursor:
                # Check if database exists first
                await cursor.execute("SHOW DATABASES LIKE %s", (self.database,))
                result = await cursor.fetchone()
                
                if result:
                    logger.info(f"✓ Database '{self.database}' already exists")
                else:
                    await cursor.execute(f"CREATE DATABASE {self.database}")
                    logger.info(f"✓ Database '{self.database}' created successfully")
            conn.close()
        except Exception as e:
            logger.warning(f"Could not create database: {e}")

    def init_db(self, app: FastAPI):
        """
        Register Tortoise ORM with FastAPI application
        """
        # We'll handle schema generation manually in init_tortoise()
        register_tortoise(
            app,
            db_url=self.db_url,
            modules=self.modules,
            generate_schemas=False,
            add_exception_handlers=True,
        )
        logger.info("Tortoise ORM registered with FastAPI (manual schema generation)")

    async def init_tortoise(self):
        """
        Initialize Tortoise ORM and generate schemas (environment-aware)
        """
        # Ensure database exists (development only)
        await self.ensure_database_exists()
        
        await Tortoise.init(
            db_url=self.db_url,
            modules=self.modules
        )
        
        # Only generate schemas in development environments
        if self.env in ["local", "dev", "stag"]:
            # Check if tables already exist to avoid warnings
            tables_exist = await self._check_tables_exist()
            
            if tables_exist:
                logger.info("Tortoise ORM initialized - tables already exist")
            else:
                await Tortoise.generate_schemas(safe=True)
                logger.info("Tortoise ORM initialized and schemas generated (development)")
        else:
            logger.info("Tortoise ORM initialized (production mode - no schema generation)")

    async def _check_tables_exist(self):
        """
        Check if required tables already exist
        """
        try:
            connect_params = {
                "host": self.host,
                "port": self.port,
                "user": self.username,
                "db": self.database,
            }
            # Only add password if it's not empty
            if self.password:
                connect_params["password"] = self.password
            
            conn = await aiomysql.connect(**connect_params)
            async with conn.cursor() as cursor:
                await cursor.execute("SHOW TABLES LIKE 'darwin_image_builder'")
                table_exists = await cursor.fetchone()
                
            conn.close()
            return bool(table_exists)
        except Exception as e:
            logger.warning(f"Could not check table existence: {e}")
            return False

    async def close_tortoise(self):
        """
        Close all database connections
        """
        await Tortoise.close_connections()
        logger.info("Tortoise ORM connections closed")
