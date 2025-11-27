import os

CONFIG_MAP = {
    "MLFLOW_UI_URL": os.getenv("MLFLOW_UI_URL", "http://localhost:8080"),
    "MLFLOW_APP_LAYER_URL": os.getenv("MLFLOW_APP_LAYER_URL", "http://localhost:8000"),
    "MLFLOW_APP_BASE_PATH": os.getenv("MLFLOW_APP_BASE_PATH", ""),
    "MLFLOW_ADMIN_USERNAME": os.getenv("VAULT_SERVICE_MLFLOW_ADMIN_USERNAME", ""),
    "MLFLOW_ADMIN_PASSWORD": os.getenv("VAULT_SERVICE_MLFLOW_ADMIN_PASSWORD", ""),
    "mysql_db": {
        "host": os.getenv("DARWIN_MYSQL_HOST", "localhost"),
        "username": os.getenv("VAULT_SERVICE_MYSQL_USERNAME", ""),
        "password": os.getenv("VAULT_SERVICE_MYSQL_PASSWORD", ""),
        "database": os.getenv("CONFIG_SERVICE_MYSQL_DATABASE", ""),
        "port": os.getenv("MYSQL_PORT", "3306"),
    },
}
