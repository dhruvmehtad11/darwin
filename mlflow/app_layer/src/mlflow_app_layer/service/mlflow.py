from typeguard import typechecked

from mlflow_app_layer.dao.auth_dao import AuthDao


@typechecked
class MLFlow:
    def __init__(self):
        self.dao = AuthDao()

    def get_experiment_user(self, experiment_id: int):
        return self.dao.get_experiment_user(experiment_id)
