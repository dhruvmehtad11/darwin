from datetime import datetime


def is_gpu(node: dict[str, any]):
    return node["node_type"] == "gpu"


def get_time_diff_seconds(start_time: datetime, end_time: datetime):
    return (end_time - start_time).total_seconds()


def get_time_diff_with_current_time(start_time: datetime):
    return get_time_diff_seconds(start_time, datetime.utcnow())
