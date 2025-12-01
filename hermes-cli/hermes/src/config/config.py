import os
from typing import Optional
from typeguard import typechecked
from hermes.src.config.constants import CONFIGS_MAP, CREDENTIALS_FILE_PATH


@typechecked
class Config:
    """
    Config class to get the configuration based on environment
    """

    def __init__(self, env: Optional[str] = None):
        if not env:
            self.env = os.getenv("ENV", "prod")
        else:
            self.env = env

        self._config = CONFIGS_MAP[self.env]

    @property
    def get_env(self):
        return self.env

    @property
    def get_hermes_deployer_url(self):
        return self._config["hermes_deployer_url"]

    @property
    def get_feature_store_url(self):
        return self._config["feature_store_url"]

    @property
    def get_user_token(self):
        self.get_user_token_from_config_file()
        if not self.user_token:
            raise ValueError("Please set the user token as an environment variable.")
        return self.user_token

    @property
    def get_hermes_deployer_url_for_env(self, env: str):
        return CONFIGS_MAP[env]["hermes_deployer_url"]

    @property
    def get_user_token_for_env(self, env: str):
        user_token = CONFIGS_MAP[env].get("user_token")
        if not user_token:
            raise ValueError("Please set the user token as an environment variable.")
        return user_token

    def get_user_token_from_config_file(self):
        """
        Read user token from .hermes/credential.txt file in the user's home directory
        Returns:
            str: User token from credentials file
        Raises:
            FileNotFoundError: If credentials file doesn't exist
            ValueError: If credentials file is empty or malformed
        """
        credentials_path = os.path.expanduser(CREDENTIALS_FILE_PATH)
        try:
            if not os.path.exists(credentials_path):
                raise FileNotFoundError(f"Credentials file not found at {credentials_path}")

            with open(credentials_path, "r") as f:
                content = f.read().strip()

            if not content:
                raise ValueError("Credentials file is empty")

            # Parse token from HERMES_USER_TOKEN=<token> format
            if not content.startswith("HERMES_USER_TOKEN="):
                raise ValueError("Invalid credentials file format. Expected HERMES_USER_TOKEN=<token>")

            token = content.split("=", 1)[1].strip()

            if not token:
                raise ValueError("Token value is empty in credentials file")

        except Exception as e:
            raise ValueError(f"Credentials not set, please run 'hermes configure' to set the credentials")

        self.user_token = token
