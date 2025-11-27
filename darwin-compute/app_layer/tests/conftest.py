import pytest

from compute_model.head_node import HeadNode
from compute_model.compute_cluster import ComputeClusterDefinition
from compute_model.worker_group import WorkerGroup
from compute_model.advance_config import AdvanceConfig


@pytest.fixture()
def search_entity():
    return {"query": "", "filters": {}, "page_size": 10, "offset": 0, "sort_by": "created_on", "sort_order": "desc"}


@pytest.fixture()
def cluster_entity():
    return {
        "cluster_name": "odin-test",
        "tags": ["test"],
        "labels": {"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"},
        "runtime": "0.0",
        "inactive_time": 60,
        "user": "test@darwin.com",
        "head_node_config": {"cores": 2, "memory": 4, "node_capacity_type": "spot", "node_type": "general"},
        "worker_node_configs": [
            {
                "cores_per_pods": 2,
                "memory_per_pods": 2,
                "min_pods": 2,
                "max_pods": 2,
                "node_type": "general",
                "node_capacity_type": "spot",
                "disk_setting": {"disk_type": "gp2", "storage_size": 16},
            }
        ],
        "advance_config": {
            "init_script": "pip install mlflow",
            "ray_params": {"object_store_memory": 25, "cpus_on_head": 2},
        },
    }


@pytest.fixture()
def request_headers():
    return {"msd-user": '{"email": "test@darwin.com"}'}


@pytest.fixture()
def update_entity():
    return {
        "cluster_name": "updated-name",
        "tags": ["jobs", "test"],
        "labels": {"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"},
        "runtime": "0.0",
        "auto_termination_policies": [
            {"policy_name": "JupyterLabActivity", "params": {}, "enabled": True},
            {
                "policy_name": "ClusterCPUUsage",
                "params": {"head_node_cpu_usage_threshold": 100, "worker_node_cpu_usage_threshold": 5},
                "enabled": True,
            },
            {"policy_name": "ActiveRayJob", "params": {}, "enabled": True},
        ],
        "inactive_time": 60,
        "user": "test@darwin.com",
        "head_node_config": {"cores": 2, "memory": 4, "node_type": "general", "node_capacity_type": "ondemand"},
        "worker_node_configs": [
            {
                "cores_per_pods": 2,
                "memory_per_pods": 2,
                "min_pods": 2,
                "max_pods": 2,
                "node_type": "general",
                "node_capacity_type": "spot",
                "disk_setting": {"disk_type": "gp2", "storage_size": 16},
            }
        ],
        "advance_config": {
            "init_script": "pip install mlflow",
            "ray_params": {"object_store_memory": 25, "cpus_on_head": 2},
        },
    }


@pytest.fixture()
def old_cluster_details_for_update():
    old_cluster_details = ComputeClusterDefinition(
        name="test_cluster_name",
        tags=["test"],
        labels={"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"},
        runtime="0.0",
        head_node=HeadNode(
            node_type="general", node={"cores": 2, "memory": 4, "disk": None, "node_capacity_type": "ondemand"}
        ),
        worker_group=[
            WorkerGroup(
                node_type="general",
                node={
                    "cores": 2,
                    "memory": 4,
                    "min_pods": 1,
                    "max_pods": 2,
                    "disk_setting": None,
                    "node_capacity_type": "ondemand",
                },
                max_pods=2,
                min_pods=1,
            )
        ],
        advance_config=AdvanceConfig(log_path="", init_script=["pip3 install awscli"], instance_role="ray"),
        user="test@darwin.com",
        is_job_cluster=False,
        start_cluster=True,
    )

    return old_cluster_details.to_dict()
