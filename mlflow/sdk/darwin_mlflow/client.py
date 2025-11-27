import os

import mlflow
from mlflow import *
from mlflow.client import MlflowClient

from darwin_mlflow.constant.config import Config

os.environ["MLFLOW_TRACKING_USERNAME"] = os.environ["user"]
os.environ["MLFLOW_TRACKING_PASSWORD"] = os.environ["user"]

config = Config()
set_tracking_uri(config.get_mlflow_tracking_uri)
mlflow_client = MlflowClient(config.get_mlflow_tracking_uri)

