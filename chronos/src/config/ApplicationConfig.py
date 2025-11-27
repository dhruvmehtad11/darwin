from os import environ

from src.constant.constants import ENV_ENVIRONMENT_VARIABLE, APP_DIR_ENVIRONMENT_VARIABLE


class ApplicationConfig:
    app_dir: str
    env: str
    config_file_suffix: str

    def __init__(self):
        self.app_dir = environ.get(APP_DIR_ENVIRONMENT_VARIABLE, ".")  # default if from root
        self.env = environ.get(ENV_ENVIRONMENT_VARIABLE, "local")
        self.config_file_suffix = environ.get(ENV_ENVIRONMENT_VARIABLE, "default")

    @staticmethod
    def get_config():
        return ApplicationConfig()
