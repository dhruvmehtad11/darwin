import mlflow
from .model_loader_interface import ModelLoaderInterface
from src.config.config import Config


# Mapping of flavor names to MLflow loader functions
FLAVOR_LOADERS = {
    "sklearn": mlflow.sklearn.load_model,
    "xgboost": mlflow.xgboost.load_model,
    "lightgbm": mlflow.lightgbm.load_model,
    "catboost": mlflow.catboost.load_model,
    "pytorch": mlflow.pytorch.load_model,
    "tensorflow": mlflow.tensorflow.load_model,
    "keras": mlflow.keras.load_model,
    "onnx": mlflow.onnx.load_model,
    "pyfunc": mlflow.pyfunc.load_model,
}


class MLflowModelLoader(ModelLoaderInterface):
    def __init__(self, config: Config):
        self.config = config
        self.model_uri = config.get_model_path
        self.flavor = config.get_model_flavor.lower()

    def load_model(self):
        """Load the MLflow model from the specified URI using the appropriate flavor loader."""
        loader = FLAVOR_LOADERS.get(self.flavor, mlflow.pyfunc.load_model)
        model = loader(self.model_uri)
        return model

    def reload_model(self):
        """Reload the MLflow model from the specified URI."""
        return self.load_model()
