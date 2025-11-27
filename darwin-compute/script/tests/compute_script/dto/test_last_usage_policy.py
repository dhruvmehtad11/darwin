import unittest
from datetime import datetime
from unittest.mock import patch

from compute_script.dto.cluster_info import ClusterInfo
from compute_script.dto.last_usage_policy import JupyterLabActivity, ClusterCPUUsage, ActiveRayJob


class TestLastUsagePolicy(unittest.TestCase):
    def setUp(self):
        self.cluster_info = ClusterInfo(cluster_id="test", dashboard_link="test", jupyter_link="test")

    @patch("requests.Session")
    def test_jupyter_no_activity(self, mock_session):
        # Test case for no activity
        mock_session.return_value.get.return_value.json.return_value = [
            {
                "execution_state": "idle",
                "last_activity": "2021-09-01T00:00:00Z",
            }
        ]
        resp = JupyterLabActivity(expiry_time=5, kernel_activity=True, terminal_activity=False).apply(self.cluster_info)
        self.assertFalse(resp)

    @patch("requests.Session")
    def test_jupyter_kernel_activity(self, mock_session):
        # Test case for kernel activity
        mock_session.return_value.get.return_value.json.return_value = [
            {
                "execution_state": "busy",
                "last_activity": "2021-09-01T00:00:00Z",
            }
        ]
        resp = JupyterLabActivity(expiry_time=5, kernel_activity=True, terminal_activity=False).apply(self.cluster_info)
        self.assertTrue(resp)

    @patch("requests.Session")
    def test_jupyter_kernel_activity_last5m(self, mock_session):
        # Test case for kernel activity in last 5 mins
        mock_session.return_value.get.return_value.json.return_value = [
            {
                "execution_state": "idle",
                "last_activity": datetime.now().isoformat(),
            }
        ]
        resp = JupyterLabActivity(expiry_time=5, kernel_activity=True, terminal_activity=False).apply(self.cluster_info)
        self.assertTrue(resp)

    @patch("requests.Session")
    def test_jupyter_no_terminal_activity(self, mock_session):
        # Test case for no terminal activity
        mock_session.return_value.get.return_value.json.return_value = [
            {
                "last_activity": "2021-09-01T00:00:00Z",
            }
        ]
        resp = JupyterLabActivity(expiry_time=5, kernel_activity=False, terminal_activity=True).apply(self.cluster_info)
        self.assertFalse(resp)

    @patch("requests.Session")
    def test_jupyter_terminal_activity(self, mock_session):
        # Test case for terminal activity
        mock_session.return_value.get.return_value.json.return_value = [
            {
                "last_activity": datetime.now().isoformat(),
            }
        ]
        resp = JupyterLabActivity(expiry_time=5, kernel_activity=False, terminal_activity=True).apply(self.cluster_info)
        self.assertTrue(resp)

    @patch("requests.get")
    def test_cluster_head_node_cpu_usage(self, mock_get):
        mock_get.return_value.json.return_value = {
            "data": {
                "summary": [
                    {
                        "hostname": "id-test-head",
                        "cpu": 30,
                        "raylet": {
                            "state": "ALIVE",
                        },
                    },
                    {
                        "hostname": "id-test-worker",
                        "cpu": 30,
                        "raylet": {
                            "state": "ALIVE",
                        },
                    },
                ]
            }
        }

        # Test case for head node cpu usage
        resp = ClusterCPUUsage().apply(self.cluster_info)
        self.assertTrue(resp)

    @patch("requests.get")
    def test_cluster_no_usage(self, mock_get):
        # Test case for no usage
        mock_get.return_value.json.return_value = {
            "data": {
                "summary": [
                    {
                        "hostname": "id-test-head",
                        "cpu": 0,
                        "raylet": {
                            "state": "ALIVE",
                        },
                    },
                    {
                        "hostname": "id-test-worker",
                        "cpu": 3,
                        "raylet": {
                            "state": "ALIVE",
                        },
                    },
                ]
            }
        }
        mock_get.return_value.json.return_value["data"]["summary"][0]["cpu"] = 0
        mock_get.return_value.json.return_value["data"]["summary"][1]["cpu"] = 3
        resp = ClusterCPUUsage().apply(self.cluster_info)
        self.assertFalse(resp)

    @patch("requests.get")
    def test_cluster_no_wg_usage(self, mock_get):
        # Test case for no workergroup
        mock_get.return_value.json.return_value["data"]["summary"] = [
            {
                "hostname": "id-test-head",
                "cpu": 5,
                "raylet": {
                    "state": "ALIVE",
                },
            }
        ]
        resp = ClusterCPUUsage(head_node_cpu_usage_threshold=10).apply(self.cluster_info)
        self.assertFalse(resp)

    @patch("requests.get")
    def test_no_active_ray_job(self, mock_get):
        mock_get.return_value.json.return_value = []

        # Test case for no active jobs
        self.assertFalse(ActiveRayJob().apply(self.cluster_info))

    @patch("requests.get")
    def test_active_ray_job(self, mock_get):
        # Test case for active jobs
        mock_get.return_value.json.return_value = [
            {
                "type": "SUBMISSION",
                "status": "RUNNING",
                "metadata": {
                    "owner": "workflow",
                },
            }
        ]
        self.assertTrue(ActiveRayJob().apply(self.cluster_info))
