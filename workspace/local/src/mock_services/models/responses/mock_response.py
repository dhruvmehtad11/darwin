from pydantic import BaseModel
from typing import List, Optional


class AutoTerminationPolicy(BaseModel):
    policy_name: str
    params: dict
    enabled: bool


class HeadNodeConfig(BaseModel):
    head_node_cores: int
    head_node_memory: int
    node_type: Optional[str]
    node_capacity_type: str
    gpu_pod: Optional[str]


class InstanceRole(BaseModel):
    instance_role_id: str
    display_name: str
    service_account_name: str


class RayStartParams(BaseModel):
    object_store_memory_perc: int
    num_cpus_on_head: int
    num_gpus_on_head: int


class SparkConfig(BaseModel):
    spark_ui_proxyRedirectUri: str
    spark_ui_proxyBase: str
    spark_metrics_conf_sink_prometheusServlet_class: str
    spark_metrics_conf_sink_prometheusServlet_path: str
    spark_metrics_conf_master_sink_prometheusServlet_path: str
    spark_metrics_conf_applications_sink_prometheusServlet_path: str
    spark_ui_prometheus_enabled: str


class AdvanceConfig(BaseModel):
    env_variables: str
    log_path: str
    init_script: str
    instance_role: InstanceRole
    availability_zone: Optional[str]
    ray_start_params: RayStartParams
    spark_config: SparkConfig


class Dashboards(BaseModel):
    status: str
    data: dict


class ClusterDetails(BaseModel):
    cluster_id: str
    name: str
    tags: List[str]
    runtime: str
    auto_termination_policies: List[AutoTerminationPolicy]
    inactive_time: int
    status: str
    user: str
    head_node_config: HeadNodeConfig
    worker_node_configs: List[dict]
    advance_config: AdvanceConfig
    dashboards: Dashboards
    created_on: str
    is_job_cluster: bool


class ClusterDashboards(BaseModel):
    jupyter_lab_url: str
    ray_dashboard_url: str
    grafana_dashboard_url: str
    spark_ui_url: str


class JupyterStart(BaseModel):
    jupyterLink: str


class ClusterStart(BaseModel):
    ClusterName: str
    DashboardLink: str
    JupyterLink: str
    MetricsLink: str


class DataSummary(BaseModel):
    summary: list[str]
