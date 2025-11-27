import os

CONFIGS_MAP = {
    "local": {
        "mysql_db": {
            "host": os.getenv("MYSQL_HOST", "localhost"),
            "username": os.getenv("MYSQL_USERNAME", "root"),
            "password": os.getenv("MYSQL_PASSWORD", ""),
            "database": os.getenv("MYSQL_DATABASE", "mlp_serve"),
            "port": os.getenv("MYSQL_PORT", "3306"),
        },
        "s3.bucket": os.getenv("S3_BUCKET", "local-bucket"),
        "app-layer-url": os.getenv("APP_LAYER_URL", "http://localhost:8000"),
    },
    "prod": {
        "mysql_db": {
            "host": os.getenv("MYSQL_HOST", ""),
            "username": os.getenv("MYSQL_USERNAME", ""),
            "password": os.getenv("MYSQL_PASSWORD", ""),
            "database": os.getenv("MYSQL_DATABASE", "mlp_serve"),
            "port": os.getenv("MYSQL_PORT", "3306"),
        },
        "s3.bucket": os.getenv("S3_BUCKET", ""),
        "app-layer-url": os.getenv("APP_LAYER_URL", ""),
    },
}
