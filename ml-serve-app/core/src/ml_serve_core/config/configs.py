import os
from typing import Optional

from typeguard import typechecked

from ml_serve_core.constants.constants import (
    CONFIGS_MAP,
    ISTIO_SERVICE_NAME,
    ENABLE_ISTIO
)


@typechecked
class Config:
    """
    Config class to get the configuration based on environment
    """

    def __init__(self):
        env = os.getenv("ENV", "local")
        self.env = env
        if self.env not in CONFIGS_MAP:
            raise ValueError(
                f"Invalid environment: {self.env}. "
                f"Valid environments are: {', '.join(CONFIGS_MAP.keys())}. "
                f"To add a new environment, update CONFIGS_MAP in constants.py"
            )
        self._config = CONFIGS_MAP[self.env]

    @property
    def dcm_url(self):
        return self._config.get("dcm_url")

    @property
    def get_artifact_builder_url(self):
        return self._config.get("artifact_builder_url")

    @property
    def get_artifact_builder_public_url(self):
        return self._config.get("artifact_builder_public_url")

    @property
    def get_darwin_workflow_url(self):
        return self._config.get("darwin_workflow_url")

    @property
    def get_istio_service_name(self):
        return ISTIO_SERVICE_NAME if ENABLE_ISTIO else None

    @property
    def is_istio_enabled(self):
        return ENABLE_ISTIO
