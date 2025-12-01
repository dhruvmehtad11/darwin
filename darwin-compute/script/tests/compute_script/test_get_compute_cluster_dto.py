import unittest
from unittest import mock

import requests
from requests import Response

from compute_script.get_compute_cluster_state import get_compute_cluster_dto


def mocked_requests_get(*args, **kwargs):
    class MockResponse(Response):
        def __init__(self, json_data, status_code):
            super().__init__()
            self.json_data = json_data
            self.status_code = status_code

    if args[0] == "http://darwin.dream11.com/id-test-jupyter":
        return MockResponse({"key": "value"}, 200)
    if args[0] == "http://darwin.dream11.com/id-test1-jupyter":
        return MockResponse({"key": "value"}, 500)
    raise requests.ConnectionError


class TestGetComputeClusterDto(unittest.TestCase):
    @mock.patch("requests.get", side_effect=mocked_requests_get)
    def test_get_compute_cluster_dto(self, mock_get):
        cluster_id = "id-test"
        k8s_resources = [
            {"Name": "head-1", "Status": "Running"},
            {"Name": "worker-1", "Status": "Running"},
            {"Name": "worker-2", "Status": "Running"},
        ]
        jupyter_link = "http://darwin.dream11.com/id-test-jupyter"
        cluster_dto = get_compute_cluster_dto(cluster_id, k8s_resources, jupyter_link)
        self.assertEqual(cluster_dto.head_node, 1)
        self.assertEqual(cluster_dto.jupyter, 1)
        self.assertEqual(cluster_dto.worker_nodes, 2)

    @mock.patch("requests.get", side_effect=mocked_requests_get)
    def test_get_compute_cluster_dto_jupyter_down(self, mock_get):
        cluster_id = "id-test1"
        k8s_resources = [
            {"Name": "head-1", "Status": "Running"},
            {"Name": "worker-1", "Status": "Running"},
            {"Name": "worker-2", "Status": "Running"},
        ]
        jupyter_link = "http://darwin.dream11.com/id-test1-jupyter"
        cluster_dto = get_compute_cluster_dto(cluster_id, k8s_resources, jupyter_link)
        self.assertEqual(cluster_dto.head_node, 1)
        self.assertEqual(cluster_dto.jupyter, 0)
        self.assertEqual(cluster_dto.worker_nodes, 2)

    @mock.patch("requests.get", side_effect=mocked_requests_get)
    def test_get_compute_cluster_dto_head_node_down(self, mock_get):
        cluster_id = "id-test2"
        k8s_resources = [
            {"Name": "head-1", "Status": "Pending"},
            {"Name": "worker-1", "Status": "Running"},
            {"Name": "worker-2", "Status": "Running"},
        ]
        jupyter_link = "http://darwin.dream11.com/id-test2-jupyter"
        cluster_dto = get_compute_cluster_dto(cluster_id, k8s_resources, jupyter_link)
        self.assertEqual(cluster_dto.head_node, 0)
        self.assertEqual(cluster_dto.jupyter, 0)
        self.assertEqual(cluster_dto.worker_nodes, 2)

    @mock.patch("requests.get", side_effect=mocked_requests_get)
    def test_get_compute_cluster_dto_no_k8s_resources(self, mock_get):
        cluster_id = "id-test3"
        k8s_resources = []
        jupyter_link = "http://darwin.dream11.com/id-test3-jupyter"
        cluster_dto = get_compute_cluster_dto(cluster_id, k8s_resources, jupyter_link)
        self.assertEqual(cluster_dto.head_node, 0)
        self.assertEqual(cluster_dto.jupyter, 0)
        self.assertEqual(cluster_dto.worker_nodes, 0)

    @mock.patch("requests.get", side_effect=mocked_requests_get)
    def test_get_compute_cluster_dto_null_resources(self, mock_get):
        cluster_id = "id-test4"
        k8s_resources = None
        jupyter_link = "http://darwin.dream11.com/id-test4-jupyter"
        cluster_dto = get_compute_cluster_dto(cluster_id, k8s_resources, jupyter_link)
        self.assertEqual(cluster_dto.head_node, 0)
        self.assertEqual(cluster_dto.jupyter, 0)
        self.assertEqual(cluster_dto.worker_nodes, 0)
