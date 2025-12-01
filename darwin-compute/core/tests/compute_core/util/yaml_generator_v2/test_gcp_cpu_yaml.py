import os
from os import path
from unittest.mock import patch

import deepdiff
import yaml

from compute_core.util.yaml_generator_v2.yaml_generator_v2 import create_yaml_v2
from compute_model.worker_group import WorkerGroup


@patch("compute_core.util.yaml_generator.RuntimeDao")
@patch("compute_core.util.yaml_generator.ClusterDao")
@patch("compute_core.util.utils.RuntimeDao")
@patch("compute_core.util.utils.RuntimeV2Dao")
def test_gcp_cpu_spot(
    mock_runtime_v2_dao, mock_runtime_dao, cluster_dao, mock_image_runtime_dao, compute_cluster_request
):
    os.environ["VAULT_SERVICE_DARWIN_METASTORE_USERNAME"] = "DUMMY_USER"
    os.environ["VAULT_SERVICE_DARWIN_METASTORE_PASSWORD"] = "DUMMY_PASSWORD"
    compute_cluster_request.cloud_env = "gcp"

    # To test spot node type
    compute_cluster_request.head_node.node_type = "general"
    compute_cluster_request.head_node.node.node_capacity_type = "spot"

    compute_cluster_request.worker_group.append(
        WorkerGroup(
            node={"cores": 1, "memory": 1, "disk": None, "node_capacity_type": "spot", "max_pods": 2},
            min_pods=1,
            max_pods=2,
            node_type="compute",
        )
    )

    mock_runtime_dao.return_value.get_runtime_image.return_value.split.return_value = ("test", "v1")
    mock_image_runtime_dao.return_value.get_runtime_image.return_value.split.return_value = "test:v1"
    cluster_dao.return_value.get_cluster_config.return_value = [{"value": 1}]
    mock_runtime_v2_dao.return_value.get_runtime_by_name.return_value = {"spark_connect": 0, "spark_auto_init": 0}

    _, values = create_yaml_v2(compute_cluster_request, [], env="test")

    with open(path.abspath(path.join(path.dirname(path.abspath(__file__)), "./mock/mock_gcp_cpu_spot_yaml.yaml"))) as f:
        mock_gcp_cpu_yaml = yaml.safe_load(f)

    diff = deepdiff.DeepDiff(mock_gcp_cpu_yaml, values, ignore_order=True)

    # ignore the difference in claimName and open ports
    if "values_changed" in diff:
        diff["values_changed"].pop("root['head']['volumes'][3]['persistentVolumeClaim']['claimName']", None)
        assert diff["values_changed"] == {}
        diff.pop("values_changed", None)

    for x in range(13, 63):
        diff["iterable_item_added"].pop(f"root['head']['ports'][{x}]", None)
    assert diff["iterable_item_added"] == {}
    diff.pop("iterable_item_added", None)

    assert diff == {}
