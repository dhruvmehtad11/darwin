import unittest

from compute_model.advance_config import AdvanceConfig
from compute_model.ray_start_params import RayStartParams


class TestAdvanceConfig(unittest.TestCase):
    def test_valid_object_creation(self):
        # Test case: Check if object creation is successful with valid types
        env_variables = "VAR1=value1,VAR2=value2"
        log_path = "/path/to/logs"
        init_script = ["script1.sh", "script2.sh"]
        instance_role = "worker"
        availability_zone = "us-west-1"
        ray_start_params = RayStartParams()
        spark_config = {"spark.master": "local[4]", "spark.app.name": "MyApp"}

        # Create the object
        config = AdvanceConfig(
            env_variables=env_variables,
            log_path=log_path,
            init_script=init_script,
            instance_role=instance_role,
            availability_zone=availability_zone,
            ray_start_params=ray_start_params,
            spark_config=spark_config,
        )

        # Assert the attributes
        self.assertEqual(config.env_variables, env_variables)
        self.assertEqual(config.log_path, log_path)
        self.assertEqual(config.init_script, init_script)
        self.assertEqual(config.instance_role, instance_role)
        self.assertEqual(config.availability_zone, availability_zone)
        self.assertEqual(config.ray_start_params, ray_start_params)
        self.assertEqual(config.spark_config, spark_config)

    def test_invalid_object_types(self):
        # Test case: Check if object creation raises TypeError for invalid types
        env_variables = {"VAR1": "value1", "VAR2": "value2"}  # Should be a string
        log_path = 1  # Should be a string
        init_script = [1, "script2.sh"]  # Should be a list of strings
        instance_role = 1  # Should be a string
        availability_zone = 1  # Should be a string
        ray_start_params = {"head_node_type": "p3.2xlarge"}  # Should be RayStartParams
        spark_config_wrong_val = {
            "spark.instance": None,
            "spark.master": 1,
            "spark.app.name": "MyApp",
        }  # Should be dict of str and str
        spark_config_wrong_key = {
            "spark.master": "local[4]",
            1: "MyApp",
        }  # Should be dict of str and str

        # Create the object
        with self.assertRaises(TypeError):
            AdvanceConfig(env_variables=env_variables)

        with self.assertRaises(TypeError):
            AdvanceConfig(log_path=log_path)

        with self.assertRaises(TypeError):
            AdvanceConfig(init_script="script1.sh")

        with self.assertRaises(TypeError):
            AdvanceConfig(init_script=init_script)

        with self.assertRaises(TypeError):
            AdvanceConfig(instance_role=instance_role)

        with self.assertRaises(TypeError):
            AdvanceConfig(availability_zone=availability_zone)

        with self.assertRaises(TypeError):
            AdvanceConfig(ray_start_params=ray_start_params)

        with self.assertRaises(TypeError):
            AdvanceConfig(spark_config="spark_config")

        with self.assertRaises(TypeError):
            AdvanceConfig(spark_config=spark_config_wrong_key)

        with self.assertRaises(TypeError):
            AdvanceConfig(spark_config=spark_config_wrong_val)

    def test_convert(self):
        # Test case: Check if convert method returns the correct dictionary
        env_variables = "VAR1=value1,VAR2=value2"
        log_path = "/path/to/logs"
        init_script = ["script1.sh", "script2.sh"]
        instance_role = "worker"
        availability_zone = "us-west-1"
        ray_start_params = RayStartParams()
        spark_config = {"spark.master": "local[4]", "spark.app.name": "MyApp"}

        # Create the object
        config = AdvanceConfig(
            env_variables=env_variables,
            log_path=log_path,
            init_script=init_script,
            instance_role=instance_role,
            availability_zone=availability_zone,
            ray_start_params=ray_start_params,
            spark_config=spark_config,
        )

        # Convert the object
        converted = config.convert()

        # Assert the converted dictionary
        self.assertEqual(converted["environment_variables"], env_variables)
        self.assertEqual(converted["log_path"], log_path)
        self.assertEqual(converted["init_script"], "\n".join(init_script))
        self.assertEqual(converted["instance_role"]["display_name"], instance_role)
        self.assertEqual(converted["ray_params"], ray_start_params.convert())
        self.assertEqual(converted["spark_config"], spark_config)
