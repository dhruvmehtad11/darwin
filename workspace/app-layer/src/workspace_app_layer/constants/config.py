from typeguard import typechecked

from workspace_app_layer.constants.constant import CONFIGS_MAP


@typechecked
class Config:
    def __init__(self, env: str):
        self._config = CONFIGS_MAP[env]

    @property
    def playground_cluster(self):
        return self._config["playground_cluster_id"]

    @property
    def log_file_root(self):
        return self._config["log_file_root"]

    @property
    def darwin_compute_url(self):
        return self._config["darwin_compute_url"]
