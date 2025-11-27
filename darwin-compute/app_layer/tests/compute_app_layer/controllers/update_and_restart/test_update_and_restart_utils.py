import pytest

from compute_app_layer.controllers.cluster.update_and_restart_cluster import is_cluster_restart_required

cluster_diffs_true_cases = [
    {"values_changed": {"root['runtime']": {"new_value": "abc", "old_value": "0.0"}}},
    {"iterable_item_added": {"root['advance_config'].init_script[1]": "pip3 " "install " "boto3"}},
    {"iterable_item_removed": {"root['advance_config'].init_script[0]": "pip3 " "install " "awscli"}},
]


@pytest.mark.parametrize("cluster_diff", cluster_diffs_true_cases)
def test_is_cluster_restart_required_true_case(cluster_diff):
    is_cluster_restart_required_val = is_cluster_restart_required(cluster_diff)
    assert is_cluster_restart_required_val is True


cluster_diff_false_cases = [
    {"values_changed": {"root['inactive_time']": {"new_value": 20, "old_value": 10}}},
    {"iterable_item_added": {"root['worker_node_configs'][1]": "new_worker_node"}},
    {"iterable_item_removed": {"root['worker_node_configs'][0]": "old_worker_node"}},
    {"values_changed": {"root['worker_node_configs'][0]['min_pods']": {"new_value": 2, "old_value": 1}}},
    {"values_changed": {"root['worker_node_configs'][0]['max_pods']": {"new_value": 1, "old_value": 2}}},
]


@pytest.mark.parametrize("cluster_diff", cluster_diff_false_cases)
def test_is_cluster_restart_required_false_case(cluster_diff):
    is_cluster_restart_required_val = is_cluster_restart_required(cluster_diff)
    assert is_cluster_restart_required_val is False
