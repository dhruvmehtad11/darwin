import datetime
import pytest

from workspace_core.dto.response import CodespaceResponse, ProjectResponse


@pytest.fixture
def compute_cluster_details():
    return {
        "cluster_id": "id-karxhuwpyipgxfdfbsf",
        "name": "vinay-p-test-kar",
        "tags": [],
        "runtime": "Ray2.8.1-Py310-Spark3.3.1-CPU",
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
        "status": "inactive",
        "user": "all@all.com",
        "head_node_config": {
            "head_node_cores": 4,
            "head_node_memory": 4,
            "node_type": None,
            "node_capacity_type": "spot",
            "gpu_pod": None,
        },
        "worker_node_configs": [
            {
                "cores": 4,
                "memory": 4,
                "min_pods": 2,
                "max_pods": 4,
                "disk_setting": None,
                "node_type": None,
                "node_capacity_type": "spot",
                "gpu_pod": None,
            }
        ],
        "advance_config": {
            "env_variables": "",
            "log_path": "",
            "init_script": "",
            "instance_role": None,
            "availability_zone": None,
            "ray_start_params": {"object_store_memory_perc": 25, "num_cpus_on_head": 0, "num_gpus_on_head": 0},
            "spark_config": {
                "spark.ui.proxyRedirectUri": "https://darwin-mlp-stag.d11dev.com",
                "spark.ui.proxyBase": "/stag-kar/id-karxhuwpyipgxfdfbsf-sparkui",
            },
        },
        "dashboards": {
            "status": "SUCCESS",
            "data": {
                "jupyter_lab_url": "https://darwin-mlp-stag.d11dev.com/stag-kar/id-karxhuwpyipgxfdfbsf-jupyter",
                "ray_dashboard_url": "https://darwin-mlp-stag.d11dev.com/stag-kar/id-karxhuwpyipgxfdfbsf-dashboard/",
                "spark_ui_url": "https://darwin-mlp-stag.d11dev.com/stag-kar/id-karxhuwpyipgxfdfbsf-sparkui/",
                "grafana_dashboard_url": "https://darwin-mlp-stag.d11dev.com/stag-kar/id-karxhuwpyipgxfdfbsf-metrics/",
            },
        },
        "created_on": "2024-07-02 12:31:46.990403+05:30",
        "is_job_cluster": False,
    }


@pytest.fixture
def codespace_details():
    return CodespaceResponse(
        id=1,
        name="test",
        project_id=1,
        updated_at=datetime.datetime(2024, 6, 24, 9, 51, 34),
        created_at=datetime.datetime(2024, 6, 24, 9, 51, 34),
        last_synced_at=datetime.datetime(2024, 6, 24, 9, 51, 34),
        user_id="test",
        cluster_id="id-test",
        jupyter_link="https://darwin-mlp-stag.d11dev.com/stag-kar/id-test-jupyter",
    )


@pytest.fixture
def project_details():
    return ProjectResponse(
        id=1,
        name="test",
        user_id="test",
        default_codespace="test",
        updated_at="2024-06-24T09:51:34Z",
        created_at="2024-06-24T09:51:34Z",
    )


@pytest.fixture
def get_cluster_dashboards():
    return {
        "jupyter_lab_url": "http://internal-k8s-istiosys-darwinin-3b6fb4fc20-1436273136.us-east-1.elb.amazonaws.com/prod-kar/id-karvcbtiwafrjqzurf6-jupyter",
        "ray_dashboard_url": "http://internal-k8s-istiosys-darwinin-3b6fb4fc20-1436273136.us-east-1.elb.amazonaws.com/prod-kar/id-karvcbtiwafrjqzurf6-dashboard/",
        "spark_ui_url": "http://internal-k8s-istiosys-darwinin-3b6fb4fc20-1436273136.us-east-1.elb.amazonaws.com/prod-kar/id-karvcbtiwafrjqzurf6-sparkui/",
        "grafana_dashboard_url": "http://internal-k8s-istiosys-darwinin-3b6fb4fc20-1436273136.us-east-1.elb.amazonaws.com/prod-kar/id-karvcbtiwafrjqzurf6-metrics/",
    }
