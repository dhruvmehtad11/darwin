import pytest

from compute_core.compute import Compute
from compute_model.compute_cluster import ComputeClusterDefinition
from compute_model.head_node import HeadNode
from compute_model.worker_group import WorkerGroup
from compute_script.dto.cluster_metadata import ClusterMetadata


@pytest.fixture
def env():
    return "local"


@pytest.fixture
def active_cluster(env):
    from compute_script.dao.sql_dao import ScriptMySQLDao

    dao = ScriptMySQLDao(env)
    cluster = dao.get_unpicked_cluster()
    if not cluster:
        pytest.skip("No available cluster")
    return cluster


@pytest.fixture
def compute_cluster_def():
    return ComputeClusterDefinition(
        name="test-cluster",
        tags=["test"],
        runtime="Ray2.5.1-Py310-Spark3.3.1-CPU",
        head_node=HeadNode({"cores": 2, "memory": 4}),
        worker_group=[WorkerGroup({"cores": 2, "memory": 4}, 1, 2)],
        auto_termination_policies=[],
    )


@pytest.fixture
def new_cluster(env, compute_cluster_def):
    compute = Compute(env)
    cluster_id = compute.create_cluster(compute_cluster_def)["cluster_id"]
    compute.start(cluster_id)
    cluster = ClusterMetadata.from_dict(compute.get_cluster_metadata(cluster_id))
    yield cluster
    compute.stop(cluster_id)
    compute.delete_cluster(cluster_id)


@pytest.fixture
def new_cluster_for_timeout_job(mocker, compute_cluster_def):
    mock_dcm_create_cluster = mocker.patch("compute_core.service.dcm.DarwinClusterManager.create_cluster")
    mock_dcm_create_cluster.side_effect = lambda cluster_id, artifact_name, compute_request: {"ClusterName": cluster_id}
    mocker.patch(
        "compute_core.service.dcm.DarwinClusterManager.start_cluster",
        return_value={"DashboardLink": "test-cluster", "JupyterLink": "test-cluster"},
    )
    mocker.patch("compute_core.service.dcm.DarwinClusterManager.stop_cluster", return_value={})
    compute = Compute("test")
    cluster_id = compute.create_cluster(compute_cluster_def)["cluster_id"]
    compute.start(cluster_id)
    yield cluster_id
    compute.stop(cluster_id)
    compute.delete_cluster(cluster_id)
