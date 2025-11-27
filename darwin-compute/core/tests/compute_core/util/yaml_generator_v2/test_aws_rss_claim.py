from os import path
from unittest.mock import patch

import deepdiff
import yaml

from compute_core.util.yaml_generator_v2.yaml_generator_v2 import create_yaml_v2


@patch("compute_core.util.yaml_generator.RuntimeDao")
@patch("compute_core.util.yaml_generator.ClusterDao")
@patch("compute_core.util.yaml_generator_v2.rss_handler.ClusterDao")
@patch("compute_core.util.utils.RuntimeDao")
@patch("compute_core.util.utils.RuntimeV2Dao")
def test_create_yaml_aws_rss(
    mock_runtime_v2_dao,
    mock_runtime_dao,
    cluster_dao,
    cluster_dao_v2_yaml_generator,
    mock_image_runtime_dao,
    compute_cluster_request,
):
    compute_cluster_request.tags.append("rss")
    mock_runtime_dao.return_value.get_runtime_image.return_value.split.return_value = ("test", "v1")
    cluster_dao.return_value.get_cluster_config.return_value = [{"value": 1}]
    mock_image_runtime_dao.return_value.get_runtime_image.return_value.split.return_value = "test:v1"
    cluster_dao_v2_yaml_generator.return_value.get_cluster_config.return_value = [{"value": 1}]
    mock_runtime_v2_dao.return_value.get_runtime_by_name.return_value = {
        "spark_connect": "False",
        "spark_auto_init": "False",
    }

    with open(path.abspath(path.join(path.dirname(path.abspath(__file__)), "./mock/mock_aws_rss.yaml"))) as f:
        mock_aws_yaml = yaml.safe_load(f)
    _, values = create_yaml_v2(compute_cluster_request, [], env="test")
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
