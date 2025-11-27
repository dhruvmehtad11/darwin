from unittest import TestCase
from unittest.mock import patch

import pytest

from compute_app_layer.models.runtime_v2 import RuntimeClassEnum, RuntimeTypeEnum, RuntimeComponent, ComponentNameEnum
from compute_core.dao.runtime_dao import RuntimeV2Dao


class TestRuntimeDao(TestCase):
    @patch("compute_core.dao.runtime_dao.MySQLDao")
    def setup(self, mock_mysql_dao):
        self.mock_mysql_dao = mock_mysql_dao.return_value
        self.runtime_v2_dao = RuntimeV2Dao()

    @pytest.fixture(autouse=True)
    def prepare_fixture(
        self, runtime_v2_request_def, runtime_v2_details_def, get_runtime_request_def, runtime_component_def
    ):
        self.runtime_v2_request_def = runtime_v2_request_def
        self.runtime_v2_details_def = runtime_v2_details_def
        self.get_runtime_request_def = get_runtime_request_def
        self.runtime_component_def = runtime_component_def

    @pytest.mark.asyncio
    async def test_get_runtime_by_id(self):
        self.mock_mysql_dao.read.return_value = [self.runtime_v2_details_def]
        result = await self.runtime_v2_dao.get_runtime_by_id(1)
        self.mock_mysql_dao.read.assert_called_once_with("V2_GET_RUNTIME_INFO_BY_ID", {"runtime_id": 1})
        self.assertEqual(result, self.runtime_v2_details_def)

    @pytest.mark.asyncio
    async def test_get_runtime_by_name(self):
        self.mock_mysql_dao.read.return_value = [self.runtime_v2_details_def]
        result = await self.runtime_v2_dao.get_runtime_by_name("test-runtime")
        self.mock_mysql_dao.read.assert_called_once_with("V2_GET_RUNTIME_INFO_BY_NAME", {"runtime": "test-runtime"})
        self.assertEqual(result, self.runtime_v2_details_def)

    @pytest.mark.asyncio
    async def test_get_runtimes_by_type(self):
        # Mock data with multiple entries and more realistic attributes
        self.mock_mysql_dao.read.return_value = [
            {"id": 1, "runtime": "test-runtime-1", "last_updated_at": "2025-04-06T12:00:00Z", "image": "image-1"},
            {"id": 2, "runtime": "test-runtime-2", "last_updated_at": "2025-04-07T12:00:00Z", "image": "image-2"},
            {"id": 3, "runtime": "test-runtime-3", "last_updated_at": "2025-04-05T12:00:00Z", "image": "image-3"},
        ]
        result = await self.runtime_v2_dao.get_runtimes_by_type(
            RuntimeClassEnum.CPU,
            "test",
            0,
            5,
            RuntimeTypeEnum.RAY_AND_SPARK,
            False,
        )
        self.mock_mysql_dao.read.assert_called_once_with(
            "V2_GET_RUNTIMES_BY_CLASS_AND_TYPE",
            {
                "class": RuntimeClassEnum.CPU,
                "type": RuntimeTypeEnum.RAY_AND_SPARK,
                "search_query": "test",
                "is_deleted": False,
                "limit": 5,
                "offset": 0,
            },
        )
        self.assertEqual(
            result,
            [
                {"id": 1, "runtime": "test-runtime-1", "last_updated_at": "2025-04-06T12:00:00Z", "image": "image-1"},
                {"id": 2, "runtime": "test-runtime-2", "last_updated_at": "2025-04-07T12:00:00Z", "image": "image-2"},
                {"id": 3, "runtime": "test-runtime-3", "last_updated_at": "2025-04-05T12:00:00Z", "image": "image-3"},
            ],
        )

    @pytest.mark.asyncio
    async def test_get_total_count_by_class(self):
        self.mock_mysql_dao.read.return_value = [{"COUNT(*)": 5}]
        result = self.runtime_v2_dao.get_total_count_by_class(RuntimeClassEnum.CPU, "test", False)
        self.mock_mysql_dao.read.assert_called_once_with(
            "V2_GET_TOTAL_COUNT_BY_CLASS", {"class": RuntimeClassEnum.CPU, "is_deleted": False, "search_query": "test"}
        )
        self.assertEqual(result, 5)

    @pytest.mark.asyncio
    async def test_create_runtime(self):
        self.mock_mysql_dao.create.return_value = [1]
        result = await self.runtime_v2_dao.create_runtime(self.runtime_v2_request_def)
        self.mock_mysql_dao.create.assert_called_once_with(
            "V2_CREATE_RUNTIME",  # Ensure this is the query expected by the DAO
            {
                "runtime": "test-runtime",
                "class": "CPU",
                "type": "Ray and Spark",
                "image": "test-image",
                "reference_link": "test-reference-link",
                "created_by": "test-user",
                "last_updated_by": "test-user",
                "spark_connect": None,
                "spark_auto_init": None,
            },
        )
        self.assertEqual(result, 1)

    @pytest.mark.asyncio
    async def test_update_runtime(self):
        self.mock_mysql_dao.update.return_value = None
        request = self.runtime_v2_request_def
        await self.runtime_v2_dao.update_runtime(1, request)
        self.mock_mysql_dao.update.assert_called_once_with(
            "V2_UPDATE_RUNTIME",
            {
                "id": 1,
                "class": request.class_,
                "type": request.type,
                "image": request.image,
                "reference_link": request.reference_link,
                "last_updated_by": request.user,
                "spark_connect": request.spark_connect,
                "spark_auto_init": request.spark_auto_init,
            },
        )

    @pytest.mark.asyncio
    async def test_set_as_default_runtime(self):
        self.mock_mysql_dao.create.return_value = None
        await self.runtime_v2_dao.set_as_default_runtime(1, "CPU")
        self.mock_mysql_dao.create.assert_called_once_with("V2_SET_DEFAULT_RUNTIME", {"runtime_id": 1, "class": "CPU"})

    @pytest.mark.asyncio
    async def test_soft_delete_runtime(self):
        self.mock_mysql_dao.update.return_value = None
        await self.runtime_v2_dao.soft_delete_runtime("test-runtime")
        self.mock_mysql_dao.update.assert_called_once_with("V2_SOFT_DELETE_RUNTIME", {"runtime": "test-runtime"})

    @pytest.mark.asyncio
    async def test_get_runtime_components(self):
        self.mock_mysql_dao.read.return_value = self.runtime_component_def
        result = await self.runtime_v2_dao.get_runtime_components(1)
        self.mock_mysql_dao.read.assert_called_once_with("V2_GET_RUNTIME_COMPONENTS_BY_ID", {"runtime_id": 1})
        self.assertEqual(result, self.runtime_component_def)

    @pytest.mark.asyncio
    async def test_delete_components(self):
        self.mock_mysql_dao.delete.return_value = None
        await self.runtime_v2_dao.delete_components(1)
        self.mock_mysql_dao.delete.assert_called_once_with("V2_DELETE_COMPONENTS", {"runtime_id": 1})

    @pytest.mark.asyncio
    async def test_insert_component(self):
        component = RuntimeComponent(name=ComponentNameEnum.PYTHON, version="1.0")
        self.mock_mysql_dao.create.return_value = None
        await self.runtime_v2_dao.insert_component(1, component)
        self.mock_mysql_dao.create.assert_called_once_with(
            "V2_INSERT_COMPONENT", {"runtime_id": 1, "name": "Python", "version": "1.0"}
        )

    @pytest.mark.asyncio
    async def test_check_if_default_runtime(self):
        self.mock_mysql_dao.read.return_value = [{"id": 1}]
        result = self.runtime_v2_dao.check_if_default_runtime(5)
        self.mock_mysql_dao.read.assert_called_once_with("V2_GET_DEFAULT_RUNTIME_BY_ID", {"runtime_id": 5})
        self.assertEqual(result, [{"id": 1}])
