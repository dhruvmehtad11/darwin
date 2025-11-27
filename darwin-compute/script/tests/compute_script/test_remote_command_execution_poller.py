from unittest import TestCase
from unittest.mock import patch

from compute_core.constant.config import Config
from compute_core.dto.cluster_resource_dto import ClusterResourceDTO
from compute_core.dto.remote_command_dto import PodCommandExecutionStatusDto
from compute_script.dto.remote_command_dto import RunningRemoteCommandDto
from compute_script.remote_command_execution_status_update import (
    get_cluster_running_pods,
    get_updated_status_for_remote_command,
)
from compute_core.dto.request.es_compute_cluster_definition import ESComputeDefinition


class TestRemoteCommandExecutionPoller(TestCase):
    @patch("compute_core.compute.Compute")
    def setUp(self, mock_compute):
        self.compute = mock_compute.return_value
        self.compute.get_cluster.return_value: ESComputeDefinition = ESComputeDefinition(
            name="id-test", runtime="test_runtime", tags=[], head_node=None, worker_group=[]
        )
        self.compute.runtime_dao.get_runtime_namespace.return_value = "test_namespace"
        self.compute.get_kube_cluster.return_value = "test_kube_cluster"
        k8s_resources_dict = {
            "Resources": [
                {"Name": "test_pod_1", "Type": "pod", "Status": "running", "Message": "test_message"},
                {"Name": "test_pod_2", "Type": "pod", "Status": "running", "Message": "test_message"},
                {"Name": "test_pod_3", "Type": "pod", "Status": "running", "Message": "test_message"},
            ]
        }
        self.Resources = [ClusterResourceDTO.from_dict(resource) for resource in k8s_resources_dict["Resources"]]
        self.compute.dcm.cluster_status.return_value = self.Resources
        self.compute.dcm.update_remote_command_execution_status.return_value = True

        rc_dict = {
            "cluster_id": "id-test",
            "execution_id": "test_execution_id",
            "status": "running",
            "target": "cluster",
        }
        self.remote_command = RunningRemoteCommandDto.from_dict(rc_dict)
        self.config = Config("test")

    def test_get_k8s_resources(self):
        resp = get_cluster_running_pods(self.compute, "test_cluster_id")
        self.assertEqual(resp, self.Resources)

    def test_get_updated_status_for_remote_command_running(self):
        pod_statuses_dict = [
            {
                "pod_name": "test_pod_1",
                "cluster_run_id": "run_id-test",
                "status": "running",
                "execution_id": "test_execution_id",
            },
            {
                "pod_name": "test_pod_2",
                "cluster_run_id": "run_id-test",
                "status": "running",
                "execution_id": "test_execution_id",
            },
        ]
        pod_statuses = [PodCommandExecutionStatusDto.from_dict(pod) for pod in pod_statuses_dict]

        resp = get_updated_status_for_remote_command(self.remote_command, self.Resources, pod_statuses, self.config)
        self.assertEqual("running", resp.status.value)

        pod_statuses_dict = [
            {
                "pod_name": "test_pod_1",
                "cluster_run_id": "run_id-test",
                "status": "success",
                "execution_id": "test_execution_id",
            },
            {
                "pod_name": "test_pod_2",
                "cluster_run_id": "run_id-test",
                "status": "success",
                "execution_id": "test_execution_id",
            },
            {
                "pod_name": "test_pod_3",
                "cluster_run_id": "run_id-test",
                "status": "running",
                "execution_id": "test_execution_id",
            },
        ]
        pod_statuses = [PodCommandExecutionStatusDto.from_dict(pod) for pod in pod_statuses_dict]
        resp = get_updated_status_for_remote_command(self.remote_command, self.Resources, pod_statuses, self.config)
        self.assertEqual("running", resp.status.value)

    def test_get_updated_status_for_remote_command_failed(self):
        pod_statuses_dict = [
            {
                "pod_name": "test_pod_1",
                "cluster_run_id": "run_id-test",
                "status": "running",
                "execution_id": "test_execution_id",
            },
            {
                "pod_name": "test_pod_2",
                "cluster_run_id": "run_id-test",
                "status": "running",
                "execution_id": "test_execution_id",
            },
            {
                "pod_name": "test_pod_3",
                "cluster_run_id": "run_id-test",
                "status": "failed",
                "execution_id": "test_execution_id",
            },
        ]
        pod_statuses = [PodCommandExecutionStatusDto.from_dict(pod) for pod in pod_statuses_dict]
        resp = get_updated_status_for_remote_command(self.remote_command, self.Resources, pod_statuses, self.config)
        self.assertEqual("failed", resp.status.value)

    def test_get_updated_status_for_remote_command_success(self):
        pod_statuses_dict = [
            {
                "pod_name": "test_pod_1",
                "cluster_run_id": "run_id-test",
                "status": "success",
                "execution_id": "test_execution_id",
            },
            {
                "pod_name": "test_pod_2",
                "cluster_run_id": "run_id-test",
                "status": "success",
                "execution_id": "test_execution_id",
            },
            {
                "pod_name": "test_pod_3",
                "cluster_run_id": "run_id-test",
                "status": "success",
                "execution_id": "test_execution_id",
            },
        ]
        pod_statuses = [PodCommandExecutionStatusDto.from_dict(pod) for pod in pod_statuses_dict]
        resp = get_updated_status_for_remote_command(self.remote_command, self.Resources, pod_statuses, self.config)
        self.assertEqual("success", resp.status.value)
