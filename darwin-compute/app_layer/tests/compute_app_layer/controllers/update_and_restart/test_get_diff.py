from compute_app_layer.controllers.cluster.update_and_restart_cluster import (
    get_diff,
)
from compute_model.advance_config import AdvanceConfig


def test_get_diff_runtime(old_cluster_details_for_update):
    new_cluster_details = old_cluster_details_for_update.copy()
    new_cluster_details["runtime"] = "abc"
    diff = get_diff(old_cluster_details_for_update, new_cluster_details)
    assert diff == {"values_changed": {"root['runtime']": {"new_value": "abc", "old_value": "0.0"}}}


def test_get_diff_advance_config_init_added(old_cluster_details_for_update):
    new_cluster_details = old_cluster_details_for_update.copy()
    new_cluster_details["advance_config"] = AdvanceConfig(
        log_path="", init_script=["pip3 install awscli", "pip3 install boto3"], instance_role="ray"
    ).to_dict()
    diff = get_diff(old_cluster_details_for_update, new_cluster_details)
    assert diff == {"iterable_item_added": {"root['advance_config']['init_script'][1]": "pip3 " "install " "boto3"}}


def test_get_diff_advance_config_init_removed(old_cluster_details_for_update):
    new_cluster_details = old_cluster_details_for_update.copy()
    new_cluster_details["advance_config"] = AdvanceConfig(log_path="", init_script=[], instance_role="ray").to_dict()
    diff = get_diff(old_cluster_details_for_update, new_cluster_details)
    assert diff == {"iterable_item_removed": {"root['advance_config']['init_script'][0]": "pip3 " "install " "awscli"}}


def test_get_diff_advance_config_instance_id_updated(old_cluster_details_for_update):
    new_cluster_details = old_cluster_details_for_update.copy()
    new_cluster_details["advance_config"] = AdvanceConfig(
        log_path="", init_script=["pip3 install awscli"], instance_role="test-role"
    ).to_dict()
    diff = get_diff(old_cluster_details_for_update, new_cluster_details)
    assert diff == {
        "values_changed": {"root['advance_config']['instance_role']": {"new_value": "test-role", "old_value": "ray"}}
    }
