import time
from datetime import datetime, timedelta
from unittest.mock import patch

from compute_core.dao.cluster_dao import ClusterDao
from compute_model.cluster_status import ClusterStatus
from compute_script.dao.sql_dao import ScriptMySQLDao
from compute_script.dto.cluster_metadata import ClusterMetadata
from compute_script.status_poller_job import (
    status_poller_job,
    eligible_for_cluster_died_to_inactive,
    eligible_for_stop_cluster_after_creation_timout,
)


def test_status_poller_job(active_cluster):
    resp = status_poller_job("stag", active_cluster)
    assert resp == "active"


def test_creating_to_active(new_cluster):
    start = time.time()
    resp = "creating"
    while time.time() - start < 300:
        time.sleep(5)
        resp = status_poller_job("stag", new_cluster)
        if resp == "active":
            assert True, f"Cluster {new_cluster.cluster_id} became active in {time.time() - start} seconds"
            return
    assert False, f"Cluster {new_cluster.cluster_id} did not become active in 5 minutes. Last status: {resp}"


# Test for eligible_for_cluster_died_to_inactive
@patch.object(ScriptMySQLDao, "get_cluster_action_time")
@patch.object(ScriptMySQLDao, "__init__", return_value=None)  # Mock the __init__ method to avoid connection setup
def test_eligible_for_cluster_died_to_inactive(mock_init, mock_get_cluster_died_time):
    # Set up the cluster metadata and status
    cluster = ClusterMetadata(
        cluster_id="id-test",
        cluster_name="test",
        artifact_id="id-test-v1",
        last_updated_at=datetime.now(),
        last_used_at=datetime.now(),
        status="inactive",
        active_cluster_runid="test-run-id",
        active_pods=0,
        available_memory=0,
    )
    new_status = ClusterStatus.cluster_died

    # Scenario 1: The cluster died time is more than 60 minutes ago
    mock_get_cluster_died_time.return_value = datetime.now() - timedelta(minutes=61)
    result = eligible_for_cluster_died_to_inactive(cluster, new_status)
    assert result is True

    # Scenario 2: The cluster died time is less than 60 minutes ago
    mock_get_cluster_died_time.return_value = datetime.now() - timedelta(minutes=30)
    result = eligible_for_cluster_died_to_inactive(cluster, new_status)
    assert result is False

    # Scenario 3: The cluster died time is None (no record found)
    mock_get_cluster_died_time.return_value = None
    result = eligible_for_cluster_died_to_inactive(cluster, new_status)
    assert result is False


@patch.object(ScriptMySQLDao, "get_cluster_action_ending_time")
@patch.object(ScriptMySQLDao, "__init__", return_value=None)  # Mock the __init__ method to avoid connection setup
def test_eligible_for_stop_cluster_after_creation_timout(mock_script_dao_init, mock_get_cluster_action_ending_time):
    # Set up the cluster metadata and status
    cluster = ClusterMetadata(
        cluster_id="id-test",
        cluster_name="test",
        artifact_id="id-test-v1",
        last_updated_at=datetime.now(),
        last_used_at=datetime.now(),
        status="inactive",
        active_cluster_runid="test-run-id",
        active_pods=0,
        available_memory=0,
    )

    # Scenario 1: Latest action time is less than 60 mins
    mock_get_cluster_action_ending_time.return_value = datetime.now() - timedelta(minutes=0)
    result = eligible_for_stop_cluster_after_creation_timout(cluster)
    assert result is False

    # Scenario 2: Latest action time is more than 60 mins
    mock_get_cluster_action_ending_time.return_value = datetime.now() - timedelta(minutes=90)
    result = eligible_for_stop_cluster_after_creation_timout(cluster)
    assert result is True
