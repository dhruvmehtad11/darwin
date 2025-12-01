import json

from requests.models import Response
from starlette.responses import JSONResponse

from local.src.mock_services.models.responses.mock_response import *


def convert_data(data, is_data_list: bool = False):
    if is_data_list:
        return [data.dict()]
    else:
        return data.dict()


def return_response(data, is_data_list: bool = False):
    resp = {"status": "SUCCESS", "data": convert_data(data, is_data_list)}

    return JSONResponse(content=resp)


def mock_cluster_details():
    return ClusterDetails(
        cluster_id="id-test",
        name="test",
        tags=[],
        runtime="test",
        auto_termination_policies=[
            AutoTerminationPolicy(policy_name="JupyterLabActivity", params={}, enabled=True),
            AutoTerminationPolicy(
                policy_name="ClusterCPUUsage",
                params={"head_node_cpu_usage_threshold": 100, "worker_node_cpu_usage_threshold": 5},
                enabled=True,
            ),
            AutoTerminationPolicy(policy_name="test", params={}, enabled=True),
        ],
        inactive_time=100,
        status="creating",
        user="test",
        head_node_config=HeadNodeConfig(
            head_node_cores=4, head_node_memory=8, node_type=None, node_capacity_type="ondemand", gpu_pod=None
        ),
        worker_node_configs=[],
        advance_config=AdvanceConfig(
            env_variables="",
            log_path="",
            init_script="test",
            instance_role=InstanceRole(instance_role_id="1", display_name="test", service_account_name="test"),
            availability_zone=None,
            ray_start_params=RayStartParams(object_store_memory_perc=25, num_cpus_on_head=0, num_gpus_on_head=0),
            spark_config=SparkConfig(
                spark_ui_proxyRedirectUri="test",
                spark_ui_proxyBase="test",
                spark_metrics_conf_sink_prometheusServlet_class="test",
                spark_metrics_conf_sink_prometheusServlet_path="test",
                spark_metrics_conf_master_sink_prometheusServlet_path="test",
                spark_metrics_conf_applications_sink_prometheusServlet_path="test",
                spark_ui_prometheus_enabled="true",
            ),
        ),
        dashboards=Dashboards(
            status="SUCCESS",
            data={
                "jupyter_lab_url": "test_jupyterlab",
                "ray_dashboard_url": "test_ray_dashboard",
                "spark_ui_url": "test_spark_ui",
                "grafana_dashboard_url": "test_grafana",
                "code_server_url": "test_code_server",
            },
        ),
        created_on="2025-01-21 17:30:03.988698+05:30",
        is_job_cluster=False,
    )


def mock_cluster_dashboards():
    return ClusterDashboards(
        spark_ui_url="http://localhost:8001/mock/spark",
        grafana_dashboard_url="http://localhost:8001/mock/grafana",
        jupyter_lab_url="http://localhost:8001/mock/jupyterlab",
        ray_dashboard_url="http://localhost:8001/mock/ray",
    )


def mock_jupyter_data():
    return JupyterStart(jupyterLink="http://localhost:8001/mock/JupyterLink")


def mock_cluster_start():
    return ClusterStart(
        ClusterName="test",
        DashboardLink="http://localhost:8001/mock/DashboardLink",
        JupyterLink="http://localhost:8001/mock/JupyterLink",
        MetricsLink="http://localhost:8001/mock/MetricsLink",
    )


def mock_data_with_summary():
    return DataSummary(summary=["test_summary"])
