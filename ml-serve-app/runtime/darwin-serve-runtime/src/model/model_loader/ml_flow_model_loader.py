import os
import mlflow

from .model_loader_interface import ModelLoaderInterface
from src.config.config import Config


class MLFlowModelLoader(ModelLoaderInterface):
    def __init__(self, config: Config):
        # Initialize the model URI and MLflow configuration
        self.mlflow = mlflow
        self.config = config
        
        if self.config.get_mlflow_tracking_username:
            os.environ["MLFLOW_TRACKING_USERNAME"] = self.config.get_mlflow_tracking_username
        
        if self.config.get_mlflow_tracking_password:
            os.environ["MLFLOW_TRACKING_PASSWORD"] = self.config.get_mlflow_tracking_password
        
        if self.config.get_mlflow_tracking_uri:
            mlflow.set_tracking_uri(uri=self.config.get_mlflow_tracking_uri)

    def load_model(self):
        # Load the MLflow model from the specified URI
        model = self.mlflow.pyfunc.load_model(model_uri=self.config.get_model_uri)
        return model

    def reload_model(self):
        # Reload the MLflow model from the specified URI
        model = self.mlflow.pyfunc.load_model(model_uri=self.config.get_model_uri)
        return model
