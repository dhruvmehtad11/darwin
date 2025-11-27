from unittest import TestCase

from compute_model.policy_definition import PolicyDefinition


class PolicyDefinitionTest(TestCase):
    def test_default_values(self):
        policy = PolicyDefinition(policy_name="ActiveRayJobs")
        self.assertEqual(policy.policy_name, "ActiveRayJobs")
        self.assertEqual(policy.params, {})
        self.assertEqual(policy.enabled, True)

    def test_object_creation_from_dict(self):
        policy = {
            "policy_name": "ActiveRayJobs",
            "params": {"param1": "value1", "param2": "value2"},
        }
        policy_obj = PolicyDefinition.from_dict(policy)
        self.assertEqual(policy_obj.policy_name, policy["policy_name"])
        self.assertEqual(policy_obj.params, policy["params"])
        self.assertEqual(policy_obj.enabled, True)

        policy["enabled"] = False
        policy_obj = PolicyDefinition.from_dict(policy)
        self.assertEqual(policy_obj.policy_name, policy["policy_name"])
        self.assertEqual(policy_obj.params, policy["params"])
        self.assertEqual(policy_obj.enabled, policy["enabled"])

    def test_update_params(self):
        policy = PolicyDefinition(policy_name="Test Policy")
        policy.params["param1"] = "value1"
        policy.params["param2"] = "value2"
        self.assertEqual(policy.params, {"param1": "value1", "param2": "value2"})

    def test_invalid_type(self):
        policy_name = 123
        with self.assertRaises(TypeError):
            PolicyDefinition(policy_name=policy_name)

        params = "Test"
        with self.assertRaises(TypeError):
            PolicyDefinition(policy_name="Test Policy", params=params)

        enabled = "True"
        with self.assertRaises(TypeError):
            PolicyDefinition(policy_name="Test Policy", enabled=enabled)

    def test_convert(self):
        policy = PolicyDefinition(
            policy_name="ActiveRayJobs",
            params={"param1": "value1", "param2": "value2"},
            enabled=False,
        )
        policy_dict = policy.convert()
        self.assertEqual(
            policy_dict,
            {
                "policy_name": "ActiveRayJobs",
                "params": {"param1": "value1", "param2": "value2"},
                "enabled": False,
            },
        )
