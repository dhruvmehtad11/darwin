from compute_app_layer.models.create_cluster import InstanceRole, AdvanceConfig


def test_instance_role_default():
    # Test default values
    request = {"id": "1", "display_name": "darwin-ds-role"}
    role = InstanceRole(**request)
    assert role.id == request["id"]
    assert role.display_name == request["display_name"]
    assert role.service_account_name == "darwin-ds-role"


def test_instance_role_with_custom_id():
    # Test with a custom id
    request = {"id": "2", "display_name": "prod-finance-server-role"}
    role = InstanceRole(**request)
    assert role.id == request["id"]
    assert role.display_name == request["display_name"]
    assert role.service_account_name == "prod-finance-server-role"


def test_instance_role_with_invalid_id():
    # Test with an invalid id that does not exist in INSTANCE_ROLE
    request = {"id": "5"}
    role = InstanceRole(**request)
    assert role.id == "5"
    assert role.display_name == "darwin-ds-role"
    assert role.service_account_name is None


def test_instance_role_without_id():
    # Test without specifying id
    request = {}
    role = InstanceRole(**request)
    assert role.id == "1"
    assert role.display_name == "darwin-ds-role"
    assert role.service_account_name == "darwin-ds-role"


def test_spark_config_default():
    # Test default values
    request = {"environment_variables": "key1=value"}
    advance_config = AdvanceConfig(**request)
    assert advance_config.environment_variables == "key1=value"
    assert advance_config.spark_config == {}


def test_spark_config():
    request = {"spark_config": {"var1": "val1", "var2": 10, "var3": True}}
    advance_config = AdvanceConfig(**request)
    assert advance_config.spark_config == request["spark_config"]
