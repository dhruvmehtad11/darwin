from typeguard import typechecked

from darwin_compute.constant.constants import CONFIGS_MAP


@typechecked
class Config:
    """
    Config class to get the configuration based on environment
    """

    def __init__(self, env: str):
        self.env = env
        self._config = CONFIGS_MAP[self.env]

    @property
    def get_compute_url(self):
        return self._config["compute_url"]
