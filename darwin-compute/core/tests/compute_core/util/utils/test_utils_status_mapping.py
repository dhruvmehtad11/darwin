from compute_core.util.utils import (
    get_backend_status_from_ui_status,
    get_ui_status_from_backend_status,
)


def test_get_backend_status_from_ui_status():
    # Test case: Checking if the returned status is correct
    ui_statuses = ["inactive", "active"]
    expected_output = [
        "inactive",
        "active",
        "worker_nodes_scaled",
        "worker_nodes_died",
    ]
    resp = get_backend_status_from_ui_status(ui_statuses)
    assert resp == expected_output


def test_get_backend_status_from_ui_status_creating():
    # Test case: Checking if the returned status is correct for creating status
    ui_statuses = ["creating"]
    expected_output = ["creating", "head_node_up", "jupyter_up", "head_node_died", "cluster_died"]
    resp = get_backend_status_from_ui_status(ui_statuses)
    assert resp == expected_output


def test_get_backend_status_from_ui_status_empty():
    # Test case: Checking if the returned status is correct for empty status
    ui_statuses = []
    expected_output = []
    resp = get_backend_status_from_ui_status(ui_statuses)
    assert resp == expected_output


def test_get_backend_status_from_ui_status_none():
    # Test case: Checking if the returned status is correct for None status
    ui_statuses = None
    expected_output = []
    resp = get_backend_status_from_ui_status(ui_statuses)
    assert resp == expected_output


def test_get_ui_status_from_backend_status():
    # Test case: Checking if the returned status is correct
    cluster_statuses = ["inactive", "creating", "head_node_up"]
    expected_output = ["inactive", "creating"]
    resp = get_ui_status_from_backend_status(cluster_statuses)
    assert resp == expected_output


def test_get_ui_status_from_backend_status_empty():
    # Test case: Checking if the returned status is correct for empty status
    cluster_statuses = []
    expected_output = []
    resp = get_ui_status_from_backend_status(cluster_statuses)
    assert resp == expected_output


def test_get_ui_status_from_backend_status_none():
    # Test case: Checking if the returned status is correct for None status
    cluster_statuses = None
    expected_output = []
    resp = get_ui_status_from_backend_status(cluster_statuses)
    assert resp == expected_output
