import os

from pydantic import BaseModel, Field
from typing import List, Optional

from compute_app_layer.models.request.library import InstallRequest
from compute_core.constant.constants import CONFIGS_MAP
from compute_model.advance_config import AdvanceConfig as AdvanceConfigModel
from compute_model.compute_cluster import ComputeClusterDefinition
from compute_model.constant.constants import INSTANCE_ROLE
from compute_model.cpu_node import CPUNode
from compute_model.disk import Disk
from compute_model.gpu_node import GPUNode
from compute_model.head_node import HeadNode
from compute_model.policy_definition import PolicyDefinition
from compute_model.ray_start_params import RayStartParams
from compute_model.worker_group import WorkerGroup


class AutoTerminationPolicy(BaseModel):
    policy_name: str
    enabled: bool
    params: dict

    def convert(self) -> PolicyDefinition:
        return PolicyDefinition(self.policy_name, self.params, self.enabled)


class DiskSetting(BaseModel):
    disk_type: str
    storage_size: int

    def convert(self) -> Disk:
        return Disk(self.disk_type, self.storage_size)


class GPUNodeConfig(BaseModel):
    name: str
    cores: int
    memory: int
    gpu_count: int
    g_ram_memory: int
    g_ram_type: str


class HeadNodeConfig(BaseModel):
    cores: int
    memory: int
    disk_setting: Optional[DiskSetting] = None
    node_type: Optional[str] = None
    node_capacity_type: str = "ondemand"
    gpu_pod: Optional[GPUNodeConfig] = None

    def convert(self) -> HeadNode:
        if self.node_type == "gpu":
            return HeadNode(
                node=GPUNode(
                    name=self.gpu_pod.name,
                    cores=self.gpu_pod.cores,
                    memory=self.gpu_pod.memory,
                    gpu_count=self.gpu_pod.gpu_count,
                    g_ram_type=self.gpu_pod.g_ram_type,
                    g_ram_memory=self.gpu_pod.g_ram_memory,
                ).to_dict(),
                node_type=self.node_type,
            )
        else:
            return HeadNode(
                node=CPUNode(
                    cores=self.cores,
                    memory=self.memory,
                    disk=self.disk_setting.convert() if self.disk_setting else None,
                    node_capacity_type=self.node_capacity_type,
                    node_type=self.node_type,
                ).to_dict(),
                node_type=self.node_type,
            )


class WorkerNodeConfig(BaseModel):
    cores_per_pods: int
    memory_per_pods: int
    min_pods: int
    max_pods: int
    disk_setting: Optional[DiskSetting]
    node_type: Optional[str] = None
    node_capacity_type: str = "ondemand"
    gpu_pod: Optional[GPUNodeConfig] = None

    def convert(self) -> WorkerGroup:
        if self.node_type == "gpu":
            return WorkerGroup(
                node=GPUNode(
                    name=self.gpu_pod.name,
                    cores=self.gpu_pod.cores,
                    memory=self.gpu_pod.memory,
                    gpu_count=self.gpu_pod.gpu_count,
                    g_ram_type=self.gpu_pod.g_ram_type,
                    g_ram_memory=self.gpu_pod.g_ram_memory,
                ).to_dict(),
                min_pods=self.min_pods,
                max_pods=self.max_pods,
                node_type=self.node_type,
            )
        else:
            return WorkerGroup(
                node=CPUNode(
                    cores=self.cores_per_pods,
                    memory=self.memory_per_pods,
                    disk=self.disk_setting.convert() if self.disk_setting else None,
                    node_capacity_type=self.node_capacity_type,
                    node_type=self.node_type,
                ).to_dict(),
                min_pods=self.min_pods,
                max_pods=self.max_pods,
                node_type=self.node_type,
            )


class InstanceRole(BaseModel):
    id: Optional[str] = Field(default="1")
    display_name: Optional[str] = Field(default="darwin-ds-role")
    service_account_name: Optional[str] = Field(init_var=False, default=None)

    def __init__(self, **data):
        super().__init__(**data)
        for row in INSTANCE_ROLE:
            if row["instance_role_id"] == self.id:
                self.display_name = row["display_name"]
                self.service_account_name = row["service_account_name"]
                break


class AvailabilityZone(BaseModel):
    id: Optional[str] = ""
    display_name: Optional[str]


class RayParams(BaseModel):
    object_store_memory: int
    cpus_on_head: int

    def convert(self) -> RayStartParams:
        return RayStartParams(
            object_store_memory_perc=self.object_store_memory,
            num_cpus_on_head=self.cpus_on_head,
        )


class AdvanceConfig(BaseModel):
    environment_variables: Optional[str] = ""
    log_path: Optional[str] = ""
    init_script: Optional[str] = ""
    instance_role: Optional[InstanceRole] = InstanceRole()
    availability_zone: Optional[AvailabilityZone] = AvailabilityZone()
    ray_params: Optional[RayParams]
    spark_config: Optional[dict] = {}

    def convert(self) -> AdvanceConfigModel:
        return AdvanceConfigModel(
            env_variables=self.environment_variables,
            log_path=self.log_path,
            init_script=self.init_script.split("\n"),
            instance_role=self.instance_role.service_account_name,
            availability_zone=self.availability_zone.id,
            ray_start_params=(self.ray_params.convert() if self.ray_params else RayStartParams()),
            spark_config={key.replace(".", "\\."): value for key, value in self.spark_config.items()},
        )


class ClusterRequest(BaseModel):
    cluster_name: str
    tags: List[str]
    labels: dict[str, str] = Field(default_factory=dict)  # TODO: Make labels necessary after tags2.0 release
    runtime: str
    inactive_time: int  # Inactive time is in the minutes
    auto_termination_policies: Optional[List[AutoTerminationPolicy]] = []
    head_node_config: HeadNodeConfig
    worker_node_configs: List[WorkerNodeConfig]
    advance_config: Optional[AdvanceConfig] = AdvanceConfig()
    user: str
    is_job_cluster: Optional[bool] = False
    start_cluster: Optional[bool] = True
    packages_clone_from: Optional[str] = None
    packages: Optional[list[InstallRequest]]

    def get_cloud_env(self):
        if "gcp" in self.tags:
            return "gcp"
        elif "eks-0" in self.tags:
            return "eks-0"
        elif "eks-1" in self.tags:
            return "eks-1"
        elif "eks-2" in self.tags:
            return "eks-2"
        return None

    def convert(self) -> ComputeClusterDefinition:
        cloud_env = self.get_cloud_env()

        if self.is_job_cluster:
            self.inactive_time = 720

        return ComputeClusterDefinition(
            name=self.cluster_name,
            tags=self.tags,
            labels=self.labels,
            runtime=self.runtime,
            head_node=self.head_node_config.convert(),
            worker_group=[worker.convert() for worker in self.worker_node_configs],
            auto_termination_policies=[policy.convert() for policy in self.auto_termination_policies],
            terminate_after_minutes=self.inactive_time,
            advance_config=self.advance_config.convert(),
            user=self.user,
            cloud_env=cloud_env,
            is_job_cluster=self.is_job_cluster,
            start_cluster=self.start_cluster,
            estimated_cost=None,
        )
