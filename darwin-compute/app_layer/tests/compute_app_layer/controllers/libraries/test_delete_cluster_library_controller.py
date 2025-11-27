import json
import unittest
from unittest.mock import patch

from fastapi.responses import JSONResponse

from compute_app_layer.controllers.libraries.libraries_controller import uninstall_libraries_controller
from compute_app_layer.models.request.library import UninstallLibrariesRequest
from compute_core.dto.library_dto import DeleteLibrariesResponseDTO
from compute_core.dto.request.es_compute_cluster_definition import ESComputeDefinition
from compute_model.head_node import HeadNode
from compute_model.worker_group import WorkerGroup


class TestDeleteClusterLibraryController(unittest.IsolatedAsyncioTestCase):

    @patch("compute_core.util.package_management.package_manager")
    @patch("compute_core.compute")
    def setUp(self, mock_compute, mock_library_manager):
        self.mock_compute = mock_compute.return_value
        self.mock_library_manager = mock_library_manager.return_value

        self.cluster_details = ESComputeDefinition(
            name="test-cluster",
            tags=["test"],
            labels={"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"},
            runtime="test-runtime",
            head_node=HeadNode(node={"cores": 2, "memory": 4}),
            worker_group=[
                WorkerGroup(min_pods=2, max_pods=5, node={"cores": 2, "memory": 4}),
                WorkerGroup(min_pods=1, max_pods=3, node={"cores": 3, "memory": 5}),
            ],
            cluster_id="id-test",
            status="active",
        )
        self.delete_request = UninstallLibrariesRequest(id=[1, 2])

    async def test_delete_cluster_library_controller_success(self):
        resp = DeleteLibrariesResponseDTO(cluster_id="cluster_id", packages=[{"id": 1}, {"id": 2}])
        self.mock_compute.get_cluster.return_value = self.cluster_details
        self.mock_library_manager.delete_cluster_library.return_value = resp

        res = await uninstall_libraries_controller(
            "cluster_id", self.delete_request, self.mock_library_manager, self.mock_compute
        )
        self.assertIsInstance(res, JSONResponse)
        self.assertEqual(res.status_code, 200)

        res = json.loads(res.body)
        expected_resp = resp.to_dict(encode_json=True)

        self.assertEqual(res["status"], "SUCCESS")
        self.assertEqual(res["data"], expected_resp)

    async def test_delete_cluster_library_controller_compute_failure(self):
        self.mock_compute.get_cluster.side_effect = Exception("error")

        res = await uninstall_libraries_controller(
            "cluster_id", self.delete_request, self.mock_library_manager, self.mock_compute
        )
        self.assertIsInstance(res, JSONResponse)
        self.assertEqual(res.status_code, 500)

        res = json.loads(res.body)
        self.assertEqual(res["data"], None)
        self.assertEqual(res["status"], "ERROR")

    async def test_delete_cluster_library_controller_lib_manager_failure(self):
        self.mock_library_manager.delete_cluster_library.side_effect = Exception("error")
        res = await uninstall_libraries_controller(
            "cluster_id", self.delete_request, self.mock_library_manager, self.mock_compute
        )
        self.assertIsInstance(res, JSONResponse)
        self.assertEqual(res.status_code, 500)

        res = json.loads(res.body)
        self.assertEqual(res["data"], None)
        self.assertEqual(res["status"], "ERROR")
