import os

from typeguard import typechecked

from darwin_mlflow.constant.constants import CONFIGS_MAP


@typechecked
class Config:
    """
    Config class to get the configuration based on environment
    """

    def __init__(self):
        self._config = CONFIGS_MAP

    @property
    def get_mlflow_app_layer_url(self):
        return self._config["mlflow_app_layer_url"]

    @property
    def get_mlflow_tracking_uri(self):
        return self._config["mlflow_tracking_uri"]

    @property
    def get_user(self):
        return os.environ["user"]
