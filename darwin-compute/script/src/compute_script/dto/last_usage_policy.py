import requests

from dataclasses import dataclass, field

from compute_core.util.utils import urljoin
from compute_script.constant.constants import CLUSTER_NODES_SUMMARY_URL, RAY_JOBS_URL
from compute_script.dto.abstract_policy import Policy
from compute_script.dto.cluster_info import ClusterInfo
from compute_script.util.recent_activity import recent_activity


@dataclass
class JupyterLabActivity(Policy):
    expiry_time: int = field(default=5)
    kernel_activity: bool = True
    terminal_activity: bool = False

    def apply(self, cluster_info: ClusterInfo) -> bool:
        """
        Checks if the Last Activity of Jupyter Lab is more than the passed expiry time
        :return:
            true: Recent activity in the jupyter lab
            false: No Recent activity in the jupyter lab
        """
        kernels_url = urljoin(cluster_info.jupyter_link, "api/kernels")
        terminals_url = urljoin(cluster_info.jupyter_link, "api/terminals")

        session = requests.Session()

        kernels_response = session.get(kernels_url)
        kernels_response.raise_for_status()
        kernels_response = kernels_response.json()

        terminals_response = session.get(terminals_url)
        terminals_response.raise_for_status()
        terminals_response = terminals_response.json()

        if self.kernel_activity:
            for kernel in kernels_response:
                if kernel["execution_state"] == "busy":
                    return True
                if recent_activity(kernel["last_activity"], self.expiry_time):
                    return True

        if self.terminal_activity:
            for terminal in terminals_response:
                if recent_activity(terminal["last_activity"], self.expiry_time):
                    return True

        return False


@dataclass
class ClusterCPUUsage(Policy):
    head_node_cpu_usage_threshold: int = 100
    worker_node_cpu_usage_threshold: int = 10

    def apply(self, cluster_info: ClusterInfo) -> bool:
        """
        Checks if the cluster has any nodes with cpu usage more than the expected cpu usage
        :return:
            true: cpu usage more than expected
            false: cpu usage not more than expected
        """
        url = urljoin(cluster_info.dashboard_link, CLUSTER_NODES_SUMMARY_URL)

        response = requests.get(url, timeout=5)
        response.raise_for_status()
        nodes_summary = response.json()["data"]["summary"]

        for node in nodes_summary:
            if node["raylet"]["state"] == "ALIVE":
                is_head_node = "head" in node["hostname"]
                cpu_usage = node["cpu"]

                if is_head_node and cpu_usage > self.head_node_cpu_usage_threshold:
                    return True
                elif not is_head_node and cpu_usage > self.worker_node_cpu_usage_threshold:
                    return True
        return False


@dataclass
class ActiveRayJob(Policy):
    def apply(self, cluster_info: ClusterInfo) -> bool:
        """
        Checks if the cluster has any active user jobs
        :return:
            true: active user jobs
            false: no active user jobs
        """
        jobs_url = urljoin(cluster_info.dashboard_link, RAY_JOBS_URL)

        response = requests.get(jobs_url, timeout=5)
        response.raise_for_status()
        jobs_response = response.json()

        for job in jobs_response:
            is_submission_job = job["type"] == "SUBMISSION"
            is_running = job["status"] == "RUNNING"

            if is_submission_job and is_running:
                return True

        return False
