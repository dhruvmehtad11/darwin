import os

os.environ["ENV"] = "uat"

import pytest
from compute_model.compute_cluster import ComputeClusterDefinition
from compute_model.head_node import HeadNode
from compute_model.worker_group import WorkerGroup
from darwin_compute import client
from loguru import logger


@pytest.fixture
def compute():
    return client


@pytest.fixture
def basic_compute_cluster_def():
    return ComputeClusterDefinition(
        name="test-cluster",
        tags=["test"],
        runtime="Ray2.37.0-Py310-Spark3.5.1-CPU",
        head_node=HeadNode({"cores": 4, "memory": 8}),
        worker_group=[WorkerGroup({"cores": 2, "memory": 4}, 1, 2)],
    )


@pytest.fixture
def updated_compute_cluster_def():
    return ComputeClusterDefinition(
        name="test-cluster-updated",
        tags=["test"],
        runtime="Ray2.37.0-Py310-Spark3.5.1-CPU",
        head_node=HeadNode({"cores": 2, "memory": 4}),
        worker_group=[WorkerGroup({"cores": 2, "memory": 4}, 1, 3)],
    )


@pytest.fixture
def compute_cluster_id(compute, basic_compute_cluster_def):
    resp = compute.create(basic_compute_cluster_def)
    logger.debug(f"Create Cluster resp: {resp}")
    assert resp["status"] == "SUCCESS"
    cluster_id = resp["data"]["cluster_id"]
    yield cluster_id
    resp = compute.stop(cluster_id)
    logger.debug(f"Stop Cluster resp: {resp}")
    resp = compute.delete(cluster_id)
    logger.debug(f"Delete Cluster resp: {resp}")
    assert resp["status"] == "SUCCESS"
