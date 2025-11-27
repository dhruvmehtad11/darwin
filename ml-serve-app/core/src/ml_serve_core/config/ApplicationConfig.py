from os import environ

from ml_serve_core.constants.constants import ENV_ENVIRONMENT_VARIABLE


class ApplicationConfig:
    app_dir: str
    env: str
    config_file_suffix: str

    def __init__(self):
        self.app_dir = ""
        self.env = environ.get(ENV_ENVIRONMENT_VARIABLE, "local")
        self.config_file_suffix = environ.get(ENV_ENVIRONMENT_VARIABLE, "local")

    @staticmethod
    def get_config():
        return ApplicationConfig()
