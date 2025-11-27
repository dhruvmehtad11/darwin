import unittest

from mlflow_sdk.mlflow_sdk import MLFlow

mlflow = MLFlow(user="harsh.a@dream11.com")


class TestMLFlow(unittest.TestCase):
    def test_mlflow(self):
        tracking_url = mlflow.get_tracking_uri()
        assert tracking_url == "http://darwin-mlflow-lib:8080"
