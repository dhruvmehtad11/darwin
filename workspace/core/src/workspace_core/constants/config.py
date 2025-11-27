from typeguard import typechecked
from workspace_core.constants.constants import CONFIGS_MAP


@typechecked
class Config:
    def __init__(self, env: str):
        self.env = env
        self._config = CONFIGS_MAP[self.env]

    def db_config(self) -> dict:
        mysql_db = self._config["mysql_db"]
        return {
            "host": mysql_db["host"],
            "user": mysql_db["username"],
            "password": mysql_db["password"],
            "database": mysql_db["database"],
            "port": mysql_db["port"],
        }

    @property
    def ray_job_location(self):
        return self._config["ray_job_location"]

    @property
    def s3_bucket(self):
        return self._config["s3_bucket"]

    @property
    def app_layer_url(self):
        return self._config["app_layer_url"]

    @property
    def get_darwin_host_internal(self):
        return self._config["darwin_host_internal"]

    @property
    def fsx_root(self):
        return self._config["fsx_root"]

    @property
    def chronos_url(self):
        return self._config["darwin_chronos_url"]
