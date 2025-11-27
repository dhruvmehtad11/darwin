import unittest
from unittest.mock import patch

from starlette.responses import JSONResponse

from compute_app_layer.controllers.libraries.libraries_controller import (
    get_libraries_controller,
    install_library_controller,
    get_library_status_controller,
    get_library_details_controller,
)
from compute_app_layer.models.request.library import SearchLibraryRequest, InstallRequest, LibraryRequest
from compute_app_layer.models.response.library import LibraryDetailsResponse, LibraryError
from compute_core.dto.library_dto import LibraryDTO, LibrarySource, LibraryStatus, LibraryType


class TestLibrariesController(unittest.IsolatedAsyncioTestCase):
    @patch("compute_core.compute")
    @patch("compute_app_layer.controllers.libraries.libraries_controller.LibraryManager")
    def setUp(self, mock_lib_manager, mock_compute):
        self.mock_lib_manager = mock_lib_manager.return_value
        self.mock_compute = mock_compute.return_value

    async def test_get_libraries_controller_success(self):
        self.mock_lib_manager.get_libraries.return_value = [
            LibraryDTO(
                cluster_id="test",
                name="test",
                source=LibrarySource.PYPI,
                status=LibraryStatus.SUCCESS,
                type=LibraryType.WHL,
                path="test",
            )
        ]
        resp = await get_libraries_controller(
            SearchLibraryRequest(cluster_id="test", key="test", sort_by="id", sort_order="asc", offset=0, page_size=10),
            self.mock_lib_manager,
        )
        self.assertIsInstance(resp, JSONResponse)

    async def test_get_libraries_controller_exception(self):
        self.mock_lib_manager.get_libraries.side_effect = Exception("ERROR")
        resp = await get_libraries_controller(
            SearchLibraryRequest(cluster_id="test", key="test", sort_by="id", sort_order="asc", offset=0, page_size=10),
            self.mock_lib_manager,
        )
        self.assertIsInstance(resp, JSONResponse)
        self.assertEqual(resp.status_code, 500)

    async def test_install_library_controller_success(self):
        self.mock_compute.get_cluster.return_value = "test"
        self.mock_lib_manager.add_library.return_value = LibraryDTO(
            cluster_id="test",
            name="test",
            source=LibrarySource.PYPI.value,
            status=LibraryStatus.SUCCESS.value,
            type=LibraryType.WHL.value,
            path="test",
            id=1,
        )
        req = InstallRequest(
            source=LibrarySource.PYPI.value,
            body=LibraryRequest(name="test", version="test", path="test"),
        )
        resp = await install_library_controller("test", req, self.mock_lib_manager, self.mock_compute)
        self.assertIsInstance(resp, JSONResponse)

    async def test_get_library_status_controller(self):
        self.mock_lib_manager.get_status.return_value = LibraryStatus.SUCCESS
        cluster_id = "test"
        library_id = "test"
        resp = await get_library_status_controller(cluster_id, library_id, self.mock_lib_manager)
        self.assertIsInstance(resp, JSONResponse)

    async def test_install_library_controller_exception(self):
        self.mock_compute.get_cluster.side_effect = Exception("ERROR")
        req = InstallRequest(
            source=LibrarySource.PYPI.value,
            body=LibraryRequest(name="test", version="test", path="test"),
        )
        resp = await install_library_controller("test", req, self.mock_lib_manager, self.mock_compute)
        self.assertIsInstance(resp, JSONResponse)
        self.assertEqual(resp.status_code, 500)

    async def test_get_library_details_controller(self):
        self.mock_lib_manager.get_library_details.return_value = LibraryDetailsResponse(
            id="test",
            version="test",
            cluster_id="test",
            name="test",
            source=LibrarySource.PYPI,
            status=LibraryStatus.SUCCESS,
            type=LibraryType.WHL,
            path="test",
            error=LibraryError(error_code="test", error_message="test"),
            content="test",
        )
        resp = await get_library_details_controller("test", "test", self.mock_lib_manager)
        self.assertIsInstance(resp, JSONResponse)
