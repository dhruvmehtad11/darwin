from compute_script.util.cluster_timeout import (
    is_gpu,
    get_time_diff_seconds,
    get_time_diff_with_current_time,
)


def test_is_gpu():
    node = {"node_type": "gpu"}
    assert is_gpu(node) is True
    node = {"node_type": "cpu"}
    assert is_gpu(node) is False


def test_get_time_diff_seconds():
    from datetime import datetime

    start_time = datetime(2021, 1, 1, 0, 0, 0)
    end_time = datetime(2021, 1, 1, 0, 0, 10)
    assert get_time_diff_seconds(start_time, end_time) == 10


def test_time_diff_with_current_time():
    from datetime import datetime

    start_time = datetime(2021, 1, 1, 0, 0, 0)
    assert get_time_diff_with_current_time(start_time) > 0
