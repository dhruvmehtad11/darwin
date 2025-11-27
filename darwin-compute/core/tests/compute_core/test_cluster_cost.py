import sys
import unittest
from compute_core.cluster_cost import PredictAPI
from compute_core.constant.constants import PredictionTables
from compute_app_layer.utils.object_diff import get_diff


class TestPredictAPI(unittest.TestCase):
    def setUp(self):
        self.api = PredictAPI()

    def test_get_price_ondemand(self):
        instance = ["t3.medium", 2, 4, 0.042, 0.013]
        price = self.api.get_price(instance, "ondemand")
        self.assertEqual(price, 0.042)

    def test_get_price_spot_below_minimum(self):
        instance = ["t3.medium", 2, 4, 0.042, 0.013]  # minimum price below 30%
        price = self.api.get_price(instance, "spot")
        self.assertEqual(price, 0.013)  # 0.3 * 0.042 = 0.0126

    def test_core_search_found(self):
        prediction_table = PredictionTables.oneTwo
        price = self.api.core_search(prediction_table, 4, "ondemand")
        self.assertEqual(price, 0.17)  # c5.xlarge has 4 cores and 0.17 price

    def test_core_search_not_found(self):
        prediction_table = PredictionTables.oneTwo
        price = self.api.core_search(prediction_table, 100, "ondemand")
        self.assertEqual(price, -1)

    def test_memory_search_found(self):
        prediction_table = PredictionTables.oneTwo
        price = self.api.memory_search(prediction_table, 4, "ondemand")
        self.assertEqual(price, 0.042)  # t3.medium has 4 GB memory and 0.042 price

    def test_memory_search_not_found(self):
        prediction_table = PredictionTables.oneTwo
        price = self.api.memory_search(prediction_table, 200, "ondemand")
        self.assertEqual(price, sys.maxsize)

    def test_cost_calc_case_one_two(self):
        price = self.api.cost_calc(2, 4, "ondemand")  # cmr = 2
        self.assertEqual(price, 0.042)  # Should return t3.medium price

    def test_cost_calc_case_two_to_four(self):
        price = self.api.cost_calc(4, 8, "ondemand")  # cmr = 2
        self.assertEqual(price, 0.17)  # Should return c5.xlarge price

    def test_cost_calc_case_four(self):
        price = self.api.cost_calc(8, 16, "ondemand")  # cmr = 4
        self.assertEqual(price, 0.34)  # Should return c5.2xlarge price

    def test_cost_calc_case_four_to_eight(self):
        price = self.api.cost_calc(4, 12, "ondemand")  # cmr = 3
        self.assertEqual(price, 0.192)  # Should return m5.xlarge price

    def test_cost_calc_case_eight(self):
        price = self.api.cost_calc(8, 64, "ondemand")  # cmr = 8
        self.assertEqual(price, 0.504)  # Should return r5.8xlarge price

    def test_cost_calc_case_above_eight(self):
        price = self.api.cost_calc(8, 80, "ondemand")  # cmr > 8
        self.assertEqual(price, 1.008)  # Should return r5.8xlarge price

    def test_get_gpu_cost_case_found(self):
        gpu_name = "NVIDIA T4"
        gpu_count = 1
        price = self.api.get_gpu_cost(gpu_name, gpu_count)
        self.assertEqual(price, 0.752)

    def test_get_gpu_cost_case_type_not_found(self):
        gpu_name = "unknown_gpu"  # unsupported gpu name
        gpu_count = 1
        with self.assertRaises(ValueError):
            self.api.get_gpu_cost(gpu_name, gpu_count)

    def test_get_gpu_cost_pricing_case_count_not_found(self):
        gpu_name = "NVIDIA T4"
        gpu_count = 8  # Unsupported count
        with self.assertRaises(ValueError):
            self.api.get_gpu_cost(gpu_name, gpu_count)

    def test_metadata_change_cpu_update(self):
        old_config = {"cpu": 4, "memory": 16}
        new_config = {"cpu": 8, "memory": 16}
        user = "test_user"
        event_request = {"request_id": "12345"}

        diff = get_diff(old_config, new_config)
        metadata = {
            "request": event_request,
            "user": user,
            "diff": diff,
        }

        print("Metadata:", metadata)

        self.assertIn("diff", metadata)
        self.assertIn("values_changed", metadata["diff"])
        self.assertEqual(metadata["diff"]["values_changed"]["root['cpu']"], {"old_value": 4, "new_value": 8})
        self.assertNotIn("root['memory']", metadata["diff"].get("values_changed", {}))
