from compute_app_layer.utils.response_util import Response
from compute_core.constant.config import Config


async def get_limits_controller(config: Config):
    data = {
        "head_node_limits": {"cores": {"min": 1, "max": 90}, "memory": {"min": 1, "max": 736}},
        "worker_node_limits": {
            "cores": config.get_cpu_node_limits["cores"],
            "memory": config.get_cpu_node_limits["memory"],
            "pods": {"min": 0},
        },
        "inactive_time_limits": {"min": 10},
        "auto_termination_policy_default_settings": {
            "time": 30,
            "head_node_threshold": 100,
            "head_node_without_worker_group_threshold": 10,
            "worker_group_threshold": 5,
            "active_ray_job": True,
            "jupyter_activity": {"kernel_activity": True, "terminal_activity": False, "expiry_time": 5},
        },
    }
    return Response.success_response(message="Limits Fetched Successfully", data=data)
