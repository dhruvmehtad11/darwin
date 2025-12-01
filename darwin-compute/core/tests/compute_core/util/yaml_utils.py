from compute_core.util.utils import add_node_selector
from compute_core.dto.node_selector_dto import NodeSelector


def test_add_node_selector():
    resource = {}
    node_selectors = [
        NodeSelector("darwin.dream11.com/resource", "ray-cluster"),
        NodeSelector("xyz", "abc"),
    ]

    add_node_selector(resource, node_selectors)
    assert resource["nodeSelector"]["darwin.dream11.com/resource"] == "ray-cluster"
    assert resource["nodeSelector"]["xyz"] == "abc"


def test_pre_filled_add_node_selector():
    resource = {"nodeSelector": {"test_name": "test_value"}}
    node_selectors = [
        NodeSelector("darwin.dream11.com/resource", "ray-cluster"),
        NodeSelector("xyz", "abc"),
    ]

    add_node_selector(resource, node_selectors)
    assert resource["nodeSelector"]["test_name"] == "test_value"
    assert resource["nodeSelector"]["darwin.dream11.com/resource"] == "ray-cluster"
    assert resource["nodeSelector"]["xyz"] == "abc"
