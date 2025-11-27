import time
from typing import Optional

from compute_core.dao.mysql_dao import MySQLDao
from compute_core.dao.queries.sql_queries import (
    INSERT_JUPYTER_POD_DETAILS,
    UPDATE_JUPYTER_LAST_ACTIVITY_DETAILS,
    GET_JUPYTER_POD_DETAILS,
    GET_UNATTACHED_JUPYTER_POD,
    GET_UNATTACHED_JUPYTER_POD_COUNT,
    GET_POD_BY_CONSUMER_ID,
    DELETE_JUPYTER_POD,
    UPDATE_JUPYTER_POD_CONSUMER,
    GET_UNUSED_JUPYTER_POD,
)


class JupyterDao:
    def __init__(self, env: str = None):
        self.mysql_dao = MySQLDao(env)

    def insert_pod_details(self, pod_name: str, jupyter_link: str, consumer_id: Optional[str]):
        data = {"pod_name": pod_name, "jupyter_link": jupyter_link, "consumer_id": consumer_id}
        return self.mysql_dao.create(INSERT_JUPYTER_POD_DETAILS, data)

    def update_pod_consumer_details(self, pod_name: str, consumer_id: str):
        data = {"pod_name": pod_name, "consumer_id": consumer_id}
        return self.mysql_dao.update(UPDATE_JUPYTER_POD_CONSUMER, data)

    def update_last_activity(self, pod_name: str):
        data = {"pod_name": pod_name}
        return self.mysql_dao.update(UPDATE_JUPYTER_LAST_ACTIVITY_DETAILS, data)

    def get_pod_details(self, pod_name: str):
        data = {"pod_name": pod_name}
        return self.mysql_dao.read(GET_JUPYTER_POD_DETAILS, data)

    def get_unattached_pod(self):
        resp = self.mysql_dao.read(GET_UNATTACHED_JUPYTER_POD)
        return resp[0] if len(resp) > 0 else None

    def get_pod_by_consumer_id(self, consumer_id: str):
        resp = self.mysql_dao.read(GET_POD_BY_CONSUMER_ID, data={"consumer_id": consumer_id})
        if len(resp) == 0:
            return None
        return resp[0]

    def delete_pod_details(self, pod_name: str):
        data = {"pod_name": pod_name}
        return self.mysql_dao.delete(DELETE_JUPYTER_POD, data)

    def get_unused_pods(self):
        resp = self.mysql_dao.read(GET_UNUSED_JUPYTER_POD)
        if len(resp) == 0:
            return None
        self.update_last_activity(resp[0]["pod_name"])
        return resp[0]

    def get_unused_pods_count(self):
        res = self.mysql_dao.read(GET_UNATTACHED_JUPYTER_POD_COUNT)
        if len(res) == 0:
            return 0
        return res[0]["COUNT(*)"]
