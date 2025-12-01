import os

from compute_script.constant.constants import CONFIGS_MAP


class Config:
    def __init__(self, env: str = None):
        if not env:
            env = os.getenv("ENV", "stag")
            if env in ["dev", "stag"]:
                env = "stag"
        self.env = env
        self._config = CONFIGS_MAP[self.env]

    @property
    def get_slack_channel(self):
        return self._config["slack_channel"]
