from mlflow_app_layer.dao.mysql_dao import MySQLDao
from mlflow_app_layer.dao.queries.sql_queries import GET_EXPERIMENT_USER


class AuthDao:
    def __init__(self):
        # Config now reads from environment variables, no env parameter needed
        self._mysql_dao = MySQLDao()

    def healthcheck(self):
        return self._mysql_dao.healthcheck()

    def get_experiment_user(self, experiment_id: int):
        query = GET_EXPERIMENT_USER
        data = {"experiment_id": experiment_id}
        return self._mysql_dao.read(query, data)
