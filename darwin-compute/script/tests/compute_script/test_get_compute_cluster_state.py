from compute_core.dto.request.es_compute_cluster_definition import ESComputeDefinition
from compute_model.head_node import HeadNode
from compute_model.worker_group import WorkerGroup
from compute_script.get_compute_cluster_state import get_compute_cluster_resources_required


def test_get_compute_cluster_resources_required():
    cluster_details = ESComputeDefinition(
        name="test-cluster",
        tags=["test"],
        runtime="test-runtime",
        head_node=HeadNode(node={"cores": 2, "memory": 4}),
        worker_group=[
            WorkerGroup(min_pods=2, max_pods=5, node={"cores": 2, "memory": 4}),
            WorkerGroup(min_pods=1, max_pods=3, node={"cores": 3, "memory": 5}),
        ],
        cluster_id="id-test",
    )
    resources_required = get_compute_cluster_resources_required(cluster_details)
    assert resources_required.worker_nodes == 3, "Not getting the correct number of worker nodes"
    assert resources_required.total_memory == 17, "Not getting the correct total memory required for the cluster"
