from compute_app_layer.models.log_central_pod_config import LogCentralPodConfig
from compute_core.util.utils import get_log_central_pod_annotation, add_custom_annotation


def test_get_log_central_pod_annotation():
    """
    Test get_log_central_pod_annotation function
    """
    log_central_pod_config = LogCentralPodConfig(
        container_name="ray-head",
        service_name="test-ray-head",
        source="ray-head",
    )
    assert get_log_central_pod_annotation(log_central_pod_config) == (
        "ad.datadoghq.com/ray-head.logs",
        '[{"source": "ray-head", "service": "test-ray-head"}]',
    )


def test_add_custom_annotation():
    """
    Test add_custom_annotation function
    """
    group = {}
    key = "test_key"
    value = "test_value"
    add_custom_annotation(group, key, value)
    assert group == {"annotations": {"test_key": "test_value"}}
