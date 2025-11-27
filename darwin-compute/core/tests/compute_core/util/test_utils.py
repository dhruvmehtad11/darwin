import unittest

from compute_core.constant.constants import (
    ResourceType,
    CLOUD_ENV_CONFIG_KEY_DEFAULT,
    CLOUD_ENV_CONFIG_KEY_JOB,
    CLOUD_ENV_CONFIG_KEY_REMOTE_KERNEL,
)
from compute_core.dto.cluster_resource_dto import RayClusterResourceDTO
from compute_core.util.utils import (
    get_random_id,
    get_ist_time,
    get_run_id,
    serialize_date,
    remove_empty_filters,
    calculate_active_resource,
    retry_with_exponential_backoff,
    get_resource_type_config_key,
)


class TestUtils(unittest.TestCase):

    def setUp(self):
        self.call_count = 0

    def test_get_random_id(self):
        # Test case: Checking if the generated ID has the correct prefix and length
        result = get_random_id("prefix-")
        self.assertTrue(result.startswith("prefix-"))
        self.assertEqual(len(result), len("prefix-") + 16)

    def test_get_ist_time(self):
        # Test case: Checking if the returned timestamp is in the correct format
        result = get_ist_time()
        self.assertRegex(result, r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}")

    def test_get_run_id(self):
        # Test case: Checking if the generated run ID has the correct prefix and length
        result = get_run_id()
        self.assertTrue(result.startswith("run_id-"))
        self.assertEqual(len(result), len("run_id-") + 16)

    async def test_retry_with_exponential_backoff_async_success_after_retries(self):
        # Test that function succeeds after some retries
        @retry_with_exponential_backoff(retries=3, delay=0.01, backoff=2.0)
        async def test_function():
            self.call_count += 1
            if self.call_count < 3:  # Fail first 2 attempts
                raise ValueError("Temporary failure")
            return "success"

        result = await test_function()
        self.assertEqual(result, "success")
        self.assertEqual(self.call_count, 3)  # Called 3 times

    async def test_retry_with_exponential_backoff_async_all_attempts_fail(self):
        # Test that exception is raised when all attempts fail
        @retry_with_exponential_backoff(retries=3, delay=0.01, backoff=2.0)
        async def test_function():
            self.call_count += 1
            raise ValueError("Always fails")

        with self.assertRaises(ValueError) as context:
            await test_function()
        self.assertEqual(str(context.exception), "Always fails")
        self.assertEqual(self.call_count, 3)  # All 3 attempts made

    def test_retry_with_exponential_backoff_sync_success_after_retries(self):
        # Test that function succeeds after some retries
        @retry_with_exponential_backoff(retries=3, delay=0.01, backoff=2.0)
        def test_function():
            self.call_count += 1
            if self.call_count < 3:  # Fail first 2 attempts
                raise ValueError("Temporary failure")
            return "success"

        result = test_function()
        self.assertEqual(result, "success")
        self.assertEqual(self.call_count, 3)  # Called 3 times

    def test_serialize_date(self):
        # Test case: Checking if the serialized date has the correct format
        date = "2023-06-01"
        result = serialize_date(date)
        self.assertEqual(result, "2023-06-01Z")

    def test_remove_empty_filters(self):
        # Test case: Removing empty filters from a dictionary with no empty filters
        filters = {"key1": ["value1", "value2"], "key2": ["value3"], "key3": []}
        result = remove_empty_filters(filters)
        self.assertEqual(result, {"key1": ["value1", "value2"], "key2": ["value3"]})

    def test_calculate_active_resources(self):
        # Test case: Checking if the active resources are calculated correctly
        result = calculate_active_resource([{"cpus": [1], "cpu": 0.5, "mem": [1, 1, 1, 1]}])
        self.assertIsInstance(result, RayClusterResourceDTO)
        self.assertEqual(result.cores_used, 0)
        self.assertEqual(result.memory_used, 100)

    def test_get_resource_type_config_key(self):
        # Test case: JOB_CLUSTER resource type returns job cluster config key
        result = get_resource_type_config_key(ResourceType.JOB_CLUSTER)
        self.assertEqual(result, CLOUD_ENV_CONFIG_KEY_JOB)

        # Test case: REMOTE_KERNEL resource type returns remote kernel config key
        result = get_resource_type_config_key(ResourceType.REMOTE_KERNEL)
        self.assertEqual(result, CLOUD_ENV_CONFIG_KEY_REMOTE_KERNEL)

        # Test case: ALL_PURPOSE_CLUSTER resource type returns default config key
        result = get_resource_type_config_key(ResourceType.ALL_PURPOSE_CLUSTER)
        self.assertEqual(result, CLOUD_ENV_CONFIG_KEY_DEFAULT)

        # Test case: Any other resource type returns default config key
        # This tests the default case when no specific condition is matched
        result = get_resource_type_config_key(ResourceType.SPARK_HISTORY_SERVER)
        self.assertEqual(result, CLOUD_ENV_CONFIG_KEY_DEFAULT)
