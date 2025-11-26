import os

CONFIGS_MAP = {
    "mlflow_app_layer_url": os.getenv("MLFLOW_APP_LAYER_URL", "http://localhost:8000"),
    "mlflow_tracking_uri": os.getenv("MLFLOW_TRACKING_URI", "http://localhost:5000"),
}
