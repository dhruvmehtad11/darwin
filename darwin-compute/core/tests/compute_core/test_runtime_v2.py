from unittest import TestCase
from unittest.mock import AsyncMock, patch

import pytest

from compute_app_layer.models.runtime_v2 import RuntimeClassEnum, RuntimeTypeEnum, RuntimeV2Details, GetRuntimesRequest
from compute_core.dao.runtime_dao import RuntimeV2Dao
from compute_core.runtime_v2 import RuntimeV2


class TestRuntimeV2(TestCase):
    @patch("compute_core.runtime_v2.Config")
    @patch("compute_core.runtime_v2.RuntimeV2Dao")
    def setUp(self, mock_runtime_v2_dao, mock_config):
        self.mock_config = mock_config.return_value
        self.mock_config.env = "stag"
        self.mock_runtime_v2_dao = mock_runtime_v2_dao.return_value
        self.runtime_v2 = RuntimeV2(env="stag")

    @pytest.fixture(autouse=True)
    def prepare_fixture(
        self, runtime_v2_request_def, runtime_v2_details_def, get_runtime_request_def, runtime_component_def
    ):
        self.runtime_v2_request_def = runtime_v2_request_def
        self.runtime_v2_details_def = runtime_v2_details_def
        self.get_runtime_request_def = get_runtime_request_def
        self.runtime_component_def = runtime_component_def

    # Test for soft_delete_runtime

    @pytest.mark.asyncio
    async def test_soft_delete_runtime_success(self):
        # Mock the DAO methods to simulate a runtime that exists and is not deleted
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = self.runtime_v2_details_def
        self.mock_runtime_v2_dao.check_if_default_runtime.return_value = False
        self.mock_runtime_v2_dao.soft_delete_runtime.return_value = None

        # Call the method
        await self.runtime_v2.soft_delete_runtime("test-runtime")

        # Assert that the correct DAO methods were called
        self.mock_runtime_v2_dao.get_runtime_by_name.assert_called_once_with("test-runtime")
        self.mock_runtime_v2_dao.check_if_default_runtime.assert_called_once_with(1)
        self.mock_runtime_v2_dao.soft_delete_runtime.assert_called_once_with("test-runtime")

    @pytest.mark.asyncio
    async def test_soft_delete_runtime_not_found(self):
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = None

        with pytest.raises(ValueError, match="Runtime: test-runtime not found. Verify runtime name and try again."):
            await self.runtime_v2.soft_delete_runtime("test-runtime")

    @pytest.mark.asyncio
    async def test_soft_delete_runtime_already_deleted(self):
        # Mock the DAO method to simulate that the runtime is already deleted
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = {
            "id": 1,
            "runtime": "test-runtime",
            "image": "test-image",
            "is_deleted": True,
        }

        # Call the method and assert that it raises the ValueError
        with pytest.raises(ValueError, match="Runtime: test-runtime is already deleted."):
            await self.runtime_v2.soft_delete_runtime("test-runtime")

    @pytest.mark.asyncio
    async def test_soft_delete_runtime_is_default(self):
        # Mock the DAO methods to simulate a runtime that is the default
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = {
            "id": 1,
            "runtime": "test-runtime",
            "image": "test-image",
            "is_deleted": False,
        }
        self.mock_runtime_v2_dao.check_if_default_runtime.return_value = True

        # Call the method and assert that it raises the ValueError
        with pytest.raises(ValueError, match="Cannot delete runtime: test-runtime as it is set as the default"):
            await self.runtime_v2.soft_delete_runtime("test-runtime")

    # Test for update_runtime

    @pytest.mark.asyncio
    async def test_update_runtime_success(self):
        # Mock the DAO methods to simulate a runtime that exists and is not deleted
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = self.runtime_v2_details_def
        self.mock_runtime_v2_dao.update_runtime.return_value = None
        self.mock_runtime_v2_dao.delete_components.return_value = None
        self.mock_runtime_v2_dao.insert_component.return_value = None

        # Call the method
        request = self.runtime_v2_request_def
        result = await self.runtime_v2.update_runtime(request)

        # Assert that the correct DAO methods were called
        self.mock_runtime_v2_dao.get_runtime_by_name.assert_called_once_with("test-runtime")
        self.mock_runtime_v2_dao.update_runtime.assert_called_once_with(1, request)
        self.mock_runtime_v2_dao.delete_components.assert_called_once_with(1)
        self.mock_runtime_v2_dao.insert_component.assert_called_any_call(1, request.components[0])
        self.mock_runtime_v2_dao.insert_component.assert_called_any_call(1, request.components[1])

        # Assert that the result is the expected runtime details
        assert result.runtime == "test-runtime"
        assert result.class_ == RuntimeClassEnum.CPU
        assert result.type == RuntimeTypeEnum.RAY_AND_SPARK
        assert len(result.components) == 3
        assert result.reference_link == "test-reference-link"
        assert result.spark_auto_init is True

    @pytest.mark.asyncio
    async def test_update_runtime_not_found(self):
        # Mock the DAO method to simulate that the runtime does not exist
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = None

        # Call the method and assert that it raises the ValueError
        with pytest.raises(ValueError, match="Runtime: test-runtime not found"):
            await self.runtime_v2.update_runtime(self.runtime_v2_request_def)

    @pytest.mark.asyncio
    async def test_update_runtime_is_deleted(self):
        # Mock the DAO method to simulate that the runtime is deleted
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = {
            "id": 1,
            "runtime": "test-runtime",
            "image": "test-image",
            "is_deleted": True,
        }

        # Call the method and assert that it raises the ValueError
        with pytest.raises(ValueError, match="Runtime: test-runtime is deleted"):
            await self.runtime_v2.update_runtime(self.runtime_v2_request_def)

    @pytest.mark.asyncio
    async def test_update_runtime_set_as_default(self):
        # Mock the DAO methods to simulate a runtime that exists and is not deleted
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = self.runtime_v2_details_def
        self.mock_runtime_v2_dao.update_runtime.return_value = None
        self.mock_runtime_v2_dao.set_as_default_runtime.return_value = None

        # Call the method
        request = self.runtime_v2_request_def
        result = await self.runtime_v2.update_runtime(request)

        # Assert that the correct DAO methods were called
        self.mock_runtime_v2_dao.get_runtime_by_name.assert_called_once_with("test-runtime")
        self.mock_runtime_v2_dao.update_runtime.assert_called_once_with(1, request)
        self.mock_runtime_v2_dao.set_as_default_runtime.assert_called_once_with(1, request.class_)

    # Test for create_runtime

    @pytest.mark.asyncio
    async def test_create_runtime_success(self):
        # Mock the DAO methods to simulate a new runtime creation
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = None
        self.mock_runtime_v2_dao.create_runtime.return_value = self.runtime_v2_details_def
        self.mock_runtime_v2_dao.insert_component.return_value = None

        # Call the method
        request = self.runtime_v2_request_def
        result = await self.runtime_v2.create_runtime(request)

        # Assert that the correct DAO methods were called
        self.mock_runtime_v2_dao.get_runtime_by_name.assert_called_once_with("test-runtime")
        self.mock_runtime_v2_dao.create_runtime.assert_called_once_with(request)
        self.mock_runtime_v2_dao.insert_component.assert_called_any_call(1, request.components[0])
        self.mock_runtime_v2_dao.insert_component.assert_called_any_call(1, request.components[1])

        # Assert that the result is the expected runtime details
        assert result.runtime == "test-runtime"
        assert result.class_ == RuntimeClassEnum.CPU
        assert result.type == RuntimeTypeEnum.RAY_AND_SPARK
        assert len(result.components) == 3
        assert result.reference_link == "test-reference-link"
        assert result.spark_auto_init is True

    @pytest.mark.asyncio
    async def test_create_runtime_already_exists(self):
        # Mock the DAO method to simulate that the runtime already exists
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = {
            "id": 1,
            "runtime": "test-runtime",
            "image": "test-image",
            "is_deleted": False,
        }

        # Call the method and assert that it raises the ValueError
        with pytest.raises(ValueError, match="Runtime test-runtime already exists"):
            await self.runtime_v2.create_runtime(self.runtime_v2_request_def)

    @pytest.mark.asyncio
    async def test_create_runtime_update_soft_deleted(self):
        # Mock the DAO methods to simulate a soft-deleted runtime
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = {
            "id": 1,
            "runtime": "test-runtime",
            "image": "test-image",
            "is_deleted": True,
        }
        self.mock_runtime_v2_dao.update_soft_deleted_runtime.return_value = None

        # Call the method
        request = self.runtime_v2_request_def
        result = await self.runtime_v2.create_runtime(request)

        # Assert that the correct DAO methods were called
        self.mock_runtime_v2_dao.get_runtime_by_name.assert_called_once_with("test-runtime")
        self.mock_runtime_v2_dao.update_soft_deleted_runtime.assert_called_once_with(1, request)

        # Assert that the result is the expected runtime ID
        assert result == 1

    @pytest.mark.asyncio
    async def test_create_runtime_set_as_default(self):
        # Mock the DAO methods to simulate a new runtime creation
        self.mock_runtime_v2_dao.get_runtime_by_name.return_value = None
        self.mock_runtime_v2_dao.create_runtime.return_value = self.runtime_v2_details_def
        self.mock_runtime_v2_dao.insert_component.return_value = None
        self.mock_runtime_v2_dao.set_as_default_runtime.return_value = None

        # Call the method
        request = self.runtime_v2_request_def
        result = await self.runtime_v2.create_runtime(request)

        # Assert that the correct DAO methods were called
        self.mock_runtime_v2_dao.get_runtime_by_name.assert_called_once_with("test-runtime")
        self.mock_runtime_v2_dao.create_runtime.assert_called_once_with(request)
        self.mock_runtime_v2_dao.insert_component.assert_called_any_call(1, request.components[0])
        self.mock_runtime_v2_dao.insert_component.assert_called_any_call(1, request.components[1])
        self.mock_runtime_v2_dao.set_as_default_runtime.assert_called_once_with(1, request.class_)

    # Test for get_runtime_details

    @pytest.mark.asyncio
    async def test_get_all_runtimes_with_class(self):
        self.mock_runtime_v2_dao.get_default_runtime_details_by_class.return_value = {"runtime_id": 1}
        self.mock_runtime_v2_dao.get_runtime_by_id.return_value = self.runtime_v2_details_def
        self.mock_runtime_v2_dao.get_runtime_components.return_value = self.runtime_component_def
        self.mock_runtime_v2_dao.get_runtimes_by_type.return_value = self.runtime_v2_request_def
        self.mock_runtime_v2_dao.get_total_count_by_class.return_value = 1

        # Call the method
        request = self.runtime_v2_request_def
        result = await self.runtime_v2.get_all_runtimes(request, user="test-user")

        # Assert that the correct DAO methods were called
        self.mock_runtime_v2_dao.get_default_runtime_details_by_class.assert_called_once_with(
            request.class_, request.is_deleted, request.search_query
        )
        self.mock_runtime_v2_dao.get_runtime_by_id.assert_called_once_with(1)
        self.mock_runtime_v2_dao.get_runtime_components.assert_called_once_with(1)
        self.mock_runtime_v2_dao.get_runtimes_by_type.assert_called_once_with(
            request.class_, request.type, request.is_deleted, request.search_query
        )
        self.mock_runtime_v2_dao.get_total_count_by_class.assert_called_once_with(
            request.class_, request.is_deleted, request.search_query
        )

        # Assert: Check that the result contains the expected runtime class
        assert len(result) == 1
        assert result[0].class_ == RuntimeClassEnum.CPU
        assert result[0].total_count == 1
        assert result[0].runtimes == [RuntimeV2Details(**self.runtime_v2_details_def)]

    @pytest.mark.asyncio
    async def test_get_all_runtimes_with_custom_class(self):
        self.mock_runtime_v2_dao.get_default_runtime_details_by_class.return_value = None
        self.mock_runtime_v2_dao.get_custom_runtimes.return_value = [self.runtime_v2_details_def]
        self.mock_runtime_v2_dao.get_runtime_components.return_value = self.runtime_component_def
        self.mock_runtime_v2_dao.get_count_by_type_for_custom_class.return_value = 1

        # Call the method
        request = self.runtime_v2_request_def
        result = await self.runtime_v2.get_all_runtimes(request, user="test-user")

        # Assert that the correct DAO methods were called
        self.mock_runtime_v2_dao.get_custom_runtimes.assert_called_once_with(
            runtime_class=RuntimeClassEnum.CUSTOM,
            created_by="test-user",
            search_query=request.search_query,
            offset=request.offset,
            page_size=request.page_size,
            type=request.type,
            is_deleted=request.is_deleted,
        )
        self.mock_runtime_v2_dao.get_runtime_components.assert_called_once_with(1)
        self.mock_runtime_v2_dao.get_count_by_type_for_custom_class.assert_called_once_with(
            created_by="test-user",
            runtime_type=request.type,
            search_query=request.search_query,
            is_deleted=request.is_deleted,
        )

        # Assert: Check that the result contains the expected runtime class
        assert len(result) == 1
        assert result[0].class_ == RuntimeClassEnum.CUSTOM
        assert result[0].total_count == 1
        assert result[0].runtimes == [RuntimeV2Details(**self.runtime_v2_details_def)]

    @pytest.mark.asyncio
    async def test_get_all_runtimes_empty_results(self):
        self.mock_runtime_v2_dao.get_default_runtime_details_by_class.return_value = None
        self.mock_runtime_v2_dao.get_runtimes_by_type.return_value = []
        self.mock_runtime_v2_dao.get_total_count_by_class.return_value = 0

        # Call the method
        request = self.get_runtime_request_def
        result = await self.runtime_v2.get_all_runtimes(request, user="test-user")

        # Assert: Ensure that no runtimes are returned
        assert len(result) == 1  # There should be one class (CPU), but no runtimes in that class
        assert result[0].total_count == 0
        assert result[0].runtimes == []

    @pytest.mark.asyncio
    async def test_get_all_runtimes_with_deleted_filter(self):
        mock_runtime = {"id": 1, "runtime": "test-runtime", "is_deleted": True, "class": RuntimeClassEnum.CPU}
        self.mock_runtime_v2_dao.get_default_runtime_details_by_class.return_value = {"runtime_id": 1}
        self.mock_runtime_v2_dao.get_runtime_by_id.return_value = mock_runtime
        self.mock_runtime_v2_dao.get_runtime_components.return_value = [{"name": "component1"}]
        self.mock_runtime_v2_dao.get_runtimes_by_type.return_value = [mock_runtime]
        self.mock_runtime_v2_dao.get_total_count_by_class.return_value = 1

        # Define the request with `is_deleted=True`
        request = GetRuntimesRequest(class_=RuntimeClassEnum.CPU, is_deleted=True, search_query="")
        # Call the method
        result = await self.runtime_v2.get_all_runtimes(request, user="test-user")

        # Assert: Ensure that only deleted runtimes are returned
        assert len(result) == 1
        assert result[0].runtimes == [RuntimeV2Details(**mock_runtime)]
        assert result[0].total_count == 1

    @pytest.mark.asyncio
    async def test_get_all_runtimes_with_search_query(self):
        mock_runtime = {"id": 1, "runtime": "test-runtime", "is_deleted": False, "class": RuntimeClassEnum.CPU}
        self.mock_runtime_v2_dao.get_default_runtime_details_by_class.return_value = {"runtime_id": 1}
        self.mock_runtime_v2_dao.get_runtime_by_id.return_value = mock_runtime
        self.mock_runtime_v2_dao.get_runtime_components.return_value = [{"name": "component1"}]
        self.mock_runtime_v2_dao.get_runtimes_by_type.return_value = [mock_runtime]
        self.mock_runtime_v2_dao.get_total_count_by_class.return_value = 1

        # Define the request with a search query
        request = GetRuntimesRequest(class_=RuntimeClassEnum.CPU, is_deleted=False, search_query="test")

        # Call the method
        result = await self.runtime_v2.get_all_runtimes(request, user="test-user")

        # Assert: Ensure that the search query is used in the DAO call
        self.mock_runtime_v2_dao.get_runtimes_by_type.assert_called_once_with(
            RuntimeClassEnum.CPU, RuntimeTypeEnum.RAY_AND_SPARK, False, "test"
        )

        # Assert: Ensure that the correct query was applied
        assert len(result) == 1
        assert result[0].runtimes == [RuntimeV2Details(**mock_runtime)]
