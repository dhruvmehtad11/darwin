from os import path
from unittest.mock import patch

import deepdiff
import yaml

from compute_core.constant.constants import KubeCluster
from compute_core.util.yaml_generator_v2.yaml_generator_v2 import create_yaml_v2
from compute_model.worker_group import WorkerGroup


@patch("compute_core.util.yaml_generator.RuntimeDao")
@patch("compute_core.util.yaml_generator.ClusterDao")
@patch("compute_core.util.utils.RuntimeDao")
@patch("compute_core.util.utils.RuntimeV2Dao")
def test_nvme_disk(mock_runtime_v2_dao, mock_runtime_dao, cluster_dao, mock_image_runtime_dao, compute_cluster_request):
    compute_cluster_request.cloud_env = KubeCluster.AWS_1.value
    compute_cluster_request.head_node.node.node_type = "disk"
    compute_cluster_request.worker_group[0].node.node_type = "disk"
    compute_cluster_request.worker_group.append(
        WorkerGroup(
            node={"cores": 2, "memory": 4, "disk": None, "node_capacity_type": "spot"},
            min_pods=1,
            max_pods=2,
            node_type="memory",
        ),
    )
    mock_runtime_dao.return_value.get_runtime_image.return_value.split.return_value = ("test", "v1")
    cluster_dao.return_value.get_cluster_config.return_value = [{"value": 1}]
    mock_image_runtime_dao.return_value.get_runtime_image.return_value.split.return_value = "test:v1"
    mock_runtime_v2_dao.return_value.get_runtime_by_name.return_value = {"spark_connect": 0, "spark_auto_init": 0}

    with open(path.abspath(path.join(path.dirname(path.abspath(__file__)), "./mock/mock_aws_cpu_nvme_ssd.yaml"))) as f:
        mock_aws_yaml = yaml.safe_load(f)

    _, values = create_yaml_v2(compute_cluster_request, None, env="test")

    diff = deepdiff.DeepDiff(mock_aws_yaml, values)
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


@patch("compute_core.util.yaml_generator.RuntimeDao")
@patch("compute_core.util.yaml_generator.ClusterDao")
@patch("compute_core.util.utils.RuntimeDao")
@patch("compute_core.util.utils.RuntimeV2Dao")
def test_ebs_autoscaler_disk(
    mock_runtime_v2_dao, mock_runtime_dao, cluster_dao, mock_image_runtime_dao, compute_cluster_request
):
    compute_cluster_request.cloud_env = KubeCluster.AWS_0.value
    compute_cluster_request.tags.append("ebs-autoscale")
    compute_cluster_request.worker_group.append(
        WorkerGroup(
            node={"cores": 2, "memory": 4, "disk": None, "node_capacity_type": "spot"},
            min_pods=1,
            max_pods=2,
            node_type="disk",
        ),
    )

    mock_runtime_dao.return_value.get_runtime_image.return_value.split.return_value = ("test", "v1")
    cluster_dao.return_value.get_cluster_config.return_value = [{"value": 1}]
    mock_image_runtime_dao.return_value.get_runtime_image.return_value.split.return_value = "test:v1"
    mock_runtime_v2_dao.return_value.get_runtime_by_name.return_value = {"spark_connect": 0, "spark_auto_init": 0}

    with open(
        path.abspath(path.join(path.dirname(path.abspath(__file__)), "./mock/mock_aws_cpu_ebs_autoscaler.yaml"))
    ) as f:
        mock_aws_yaml = yaml.safe_load(f)

    _, values = create_yaml_v2(compute_cluster_request, None, env="test")

    diff = deepdiff.DeepDiff(mock_aws_yaml, values)
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
