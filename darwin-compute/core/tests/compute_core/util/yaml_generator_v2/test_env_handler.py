import unittest

import pytest

from compute_core.util.yaml_generator_v2.env_handler import (
    add_env_variable_excluding_mandatory_env,
    add_gcp_env_variable,
)


class TestEnvHandler(unittest.TestCase):
    @pytest.fixture(autouse=True)
    def prepare_fixture(self, compute_cluster_request):
        self.compute_cluster_request = compute_cluster_request

    def setUp(self):
        self.mandatory_env = [{"name": "test_env", "value": "test_value"}]

    def test_add_env_variable_excluding_mandatory_env_success(self):
        environ = []
        add_env_variable_excluding_mandatory_env(self.compute_cluster_request, self.mandatory_env, environ)
        expected = [{"name": "TEST_ENV", "value": "test"}, {"name": "TEST_ENV2", "value": "test2"}]
        self.assertEqual(expected, environ)

    def test_add_env_variable_excluding_mandatory_env_failure(self):
        environ = []
        self.mandatory_env.append({"name": "TEST_ENV", "value": "test"})
        with self.assertRaises(Exception) as context:
            add_env_variable_excluding_mandatory_env(self.compute_cluster_request, self.mandatory_env, environ)

        self.assertEqual(str(context.exception), "Predefined environment cannot be overridden")

    def test_add_gcp_env_variable(self):
        gcp_env = [{"name": "TEST_ENV", "value": "test_value"}]
        add_gcp_env_variable(self.compute_cluster_request, gcp_env)
        # only env variables in gcp_env will be updated
        expected = [{"name": "TEST_ENV", "value": "test"}]
        self.assertEqual(expected, gcp_env)
