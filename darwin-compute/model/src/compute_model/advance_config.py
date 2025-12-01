from dataclasses import dataclass, field
from typing import List, Dict, Any

from dataclasses_json import DataClassJsonMixin

from compute_model.ray_start_params import RayStartParams


@dataclass
class AdvanceConfig(DataClassJsonMixin):
    """
    Advance configuration for compute cluster.
    Args:
        env_variables: Environment variables to be set in compute cluster,
        log_path: Path to store logs in compute cluster,
        init_script: List of init scripts to be run in compute cluster,
        instance_role: Instance role of compute cluster,
        availability_zone: Availability zone of compute cluster,
        ray_start_params: Ray start parameters for compute cluster,
        spark_config: Spark configuration for compute cluster
    """

    env_variables: str = ""
    log_path: str = ""
    init_script: List[str] = field(default_factory=lambda: [])
    instance_role: str = "darwin-ds-role"
    availability_zone: str = ""
    ray_start_params: RayStartParams = RayStartParams()
    spark_config: Dict[str, Any] = field(default_factory=lambda: {})

    def __post_init__(self):
        if not isinstance(self.env_variables, str):
            raise TypeError("env_variables must be str")
        if not isinstance(self.log_path, str):
            raise TypeError("log_path must be str")
        if not isinstance(self.init_script, List):
            raise TypeError("init_script must be list")
        if not all(isinstance(item, str) for item in self.init_script):
            raise TypeError("init_script must be list of str")
        if not isinstance(self.instance_role, str):
            raise TypeError("instance_role must be str")
        if not isinstance(self.availability_zone, str):
            raise TypeError("availability_zone must be str")
        if not isinstance(self.ray_start_params, RayStartParams):
            raise TypeError("ray_start_params must be RayStartParams")
        if not isinstance(self.spark_config, Dict):
            raise TypeError("spark_config must be dict")
        if not all(isinstance(key, str) for key in self.spark_config.keys()):
            raise TypeError("spark_config must be dict of str and str/int/float/boolean")
        if not all(isinstance(value, (str, int, float, bool)) for value in self.spark_config.values()):
            raise TypeError("spark_config must be dict of str and str/int/float/boolean")

    def convert(self):
        """
        Convert AdvanceConfig to app layer dict.
        Returns:
            Dict[Any, Any]: Dict representation of AdvanceConfig
        """
        return {
            "environment_variables": self.env_variables,
            "log_path": self.log_path,
            "init_script": "\n".join(self.init_script),
            "instance_role": {"id": 1, "display_name": self.instance_role, "service_account_name": self.instance_role},
            "ray_params": self.ray_start_params.convert(),
            "spark_config": self.spark_config,
        }
