from typing import Optional, List

from sqlalchemy.sql.functions import count

from compute_app_layer.models.response.runtime_v2 import RuntimesByType
from compute_app_layer.models.runtime_v2 import (
    RuntimeClassEnum,
    RuntimeV2Request,
    RuntimeComponent,
    RuntimeV2Details,
    RuntimeTypeEnum,
)
from compute_core.constant.constants import DEFAULT_NAMESPACE
from compute_core.dao.mysql_dao import MySQLDao
from compute_core.dao.queries.sql_queries import (
    GET_ALL_RUNTIMES,
    GET_RUNTIME_INFO,
    INSERT_RUNTIME,
    UPDATE_RUNTIME_NAMESPACE,
    UPDATE_RUNTIME_IMAGE,
    DELETE_RUNTIME,
    GET_RUNTIME_IMAGE,
    GET_RUNTIME_NAMESPACE,
    V2_GET_DEFAULT_RUNTIME_BY_CLASS,
    V2_GET_RUNTIME_INFO_BY_ID,
    V2_GET_RUNTIME_COMPONENTS_BY_ID,
    V2_GET_RUNTIMES_BY_CLASS_AND_TYPE,
    V2_GET_TOTAL_COUNT_BY_CLASS,
    V2_GET_RUNTIME_INFO_BY_NAME,
    V2_UPDATE_RUNTIME,
    V2_DELETE_COMPONENTS,
    V2_SOFT_DELETE_RUNTIME,
    V2_INSERT_COMPONENT,
    V2_SET_DEFAULT_RUNTIME,
    V2_UPDATE_SOFT_DELETED_RUNTIME,
    V2_CREATE_RUNTIME,
    V2_GET_RUNTIMES_BY_USER,
    V2_GET_RUNTIMES_BY_OTHERS,
    V2_GET_DEFAULT_RUNTIME_BY_ID,
    V2_GET_COUNT_BY_CLASS_AND_TYPE,
    V2_GET_COUNT_OF_RUNTIMES_BY_USER,
    V2_GET_COUNT_OF_RUNTIMES_BY_OTHERS,
)


class RuntimeDao:
    def __init__(self, env: str = None):
        self.mysql_dao = MySQLDao(env)

    def get_all_runtimes(self):
        """
        Gives all available runtimes
        :return: List[runtimes]
        """
        query = GET_ALL_RUNTIMES
        result = self.mysql_dao.read(query)
        return result

    def get_runtime_info(self, runtime: str):
        """
        Gives info of given runtime
        :param runtime: Cluster Runtime.
        :return: runtime info including runtime, image and namespace
        """
        query = GET_RUNTIME_INFO
        data = {"runtime": runtime}
        result = self.mysql_dao.read(query, data)
        if not len(result):
            raise Exception("Runtime given is incorrect")
        return result[0]

    def get_runtime_image(self, runtime: str):
        query = GET_RUNTIME_IMAGE
        data = {"runtime": runtime}
        result = self.mysql_dao.read(query, data)
        if not len(result):
            raise Exception("Runtime given is incorrect")
        image = result[0]["image"]
        return image

    # To-do: Remove this method once we have a proper namespace management.
    # The commented code below refers to the old way of fetching namespace from the database.
    def get_runtime_namespace(self, runtime):
        # query = GET_RUNTIME_NAMESPACE
        # data = {"runtime": runtime}
        # result = self.mysql_dao.read(query, data)
        # if not len(result):
        #     raise Exception("Runtime given is incorrect")
        # ns = result[0]["namespace"]
        # return ns
        return DEFAULT_NAMESPACE

    def create_runtime(self, runtime: str, image: str, namespace: str):
        query = INSERT_RUNTIME
        data = {"runtime": runtime, "image": image, "namespace": namespace}
        result = self.mysql_dao.create(query, data)
        return result

    def update_runtime_namespace(self, runtime: str, new_namespace: str):
        query = UPDATE_RUNTIME_NAMESPACE
        data = {"runtime": runtime, "namespace": new_namespace}
        result = self.mysql_dao.update(query, data)
        return result

    def update_runtime_image(self, runtime: str, new_image: str):
        query = UPDATE_RUNTIME_IMAGE
        data = {"runtime": runtime, "image": new_image}
        result = self.mysql_dao.update(query, data)
        return result

    def delete_runtime(self, runtime: str):
        query = DELETE_RUNTIME
        data = {"runtime": runtime}
        result = self.mysql_dao.delete(query, data)
        return result


class RuntimeV2Dao:
    def __init__(self, env: str = None):
        self.mysql_dao = MySQLDao(env)

    async def get_runtime_components(self, runtime_id: int):
        query = V2_GET_RUNTIME_COMPONENTS_BY_ID
        data = {"runtime_id": runtime_id}
        components = self.mysql_dao.read(query, data)
        return components if components else None

    async def get_runtime_by_id(self, runtime_id: int):
        query = V2_GET_RUNTIME_INFO_BY_ID
        data = {"runtime_id": runtime_id}
        runtime = self.mysql_dao.read(query, data)
        return runtime[0] if runtime else None

    def get_runtime_by_name(self, runtime: str):
        query = V2_GET_RUNTIME_INFO_BY_NAME
        data = {"runtime": runtime}
        result = self.mysql_dao.read(query, data)
        return result[0] if result else None

    def delete_components(self, runtime_id: int):
        query = V2_DELETE_COMPONENTS
        data = {"runtime_id": runtime_id}
        self.mysql_dao.delete(query, data)

    def insert_component(self, runtime_id: int, component: RuntimeComponent):
        query = V2_INSERT_COMPONENT
        data = {"runtime_id": runtime_id, "name": component.name, "version": component.version}
        self.mysql_dao.create(query, data)

    def get_count_by_class_and_type(
        self,
        runtime_class: RuntimeClassEnum,
        search_query: str,
        type: RuntimeTypeEnum,
        is_deleted: bool,
    ):
        query = V2_GET_COUNT_BY_CLASS_AND_TYPE
        data = {
            "class": runtime_class,
            "type": type,
            "search_query": search_query,
            "is_deleted": is_deleted,
        }
        count = self.mysql_dao.read(query, data)
        return count[0]["COUNT(*)"]

    def get_count_by_type_for_custom_class(
        self, created_by: str, runtime_type: str, search_query: str, is_deleted: bool
    ):
        if runtime_type == RuntimeTypeEnum.CREATED_BY_ME:
            query = V2_GET_COUNT_OF_RUNTIMES_BY_USER
        else:
            query = V2_GET_COUNT_OF_RUNTIMES_BY_OTHERS
        data = {"created_by": created_by, "is_deleted": is_deleted, "search_query": search_query}
        count = self.mysql_dao.read(query, data)
        return count[0]["COUNT(*)"]

    # GET RUNTIMES
    def get_default_runtime_details_by_class(self, runtime_class: str):
        query = V2_GET_DEFAULT_RUNTIME_BY_CLASS
        data = {"class": runtime_class}
        default = self.mysql_dao.read(query, data)
        return default[0] if default else None

    def get_runtimes_by_type(
        self,
        runtime_class: RuntimeClassEnum,
        search_query: str,
        offset: int,
        page_size: int,
        type: RuntimeTypeEnum,
        is_deleted: bool,
    ):
        query = V2_GET_RUNTIMES_BY_CLASS_AND_TYPE
        data = {
            "class": runtime_class,
            "type": type,
            "search_query": search_query,
            "is_deleted": is_deleted,
            "limit": page_size,
            "offset": offset,
        }
        runtimes = self.mysql_dao.read(query, data)
        return runtimes

    def get_custom_runtimes(
        self,
        runtime_class: RuntimeClassEnum,
        created_by: str,
        search_query: str,
        offset: int,
        page_size: int,
        is_deleted: bool,
        type: RuntimeTypeEnum,
    ):
        data = {
            "class": runtime_class,
            "created_by": created_by,
            "search_query": search_query,
            "is_deleted": is_deleted,
            "limit": page_size,
            "offset": offset,
        }
        if type == RuntimeTypeEnum.CREATED_BY_ME:
            query = V2_GET_RUNTIMES_BY_USER
        else:
            query = V2_GET_RUNTIMES_BY_OTHERS
        runtimes = self.mysql_dao.read(query, data)
        return runtimes

    def get_total_count_by_class(
        self,
        runtime_class: RuntimeClassEnum,
        search_query: str,
        is_deleted: bool,
    ):
        query = V2_GET_TOTAL_COUNT_BY_CLASS
        data = {"class": runtime_class, "is_deleted": is_deleted, "search_query": search_query}
        count = self.mysql_dao.read(query, data)
        return count[0]["COUNT(*)"]

    # SOFT DELETE RUNTIME
    def check_if_default_runtime(self, runtime_id: int):
        query = V2_GET_DEFAULT_RUNTIME_BY_ID
        data = {"runtime_id": runtime_id}
        result = self.mysql_dao.read(query, data)
        return result

    def soft_delete_runtime(self, runtime: str):
        query = V2_SOFT_DELETE_RUNTIME
        data = {"runtime": runtime}
        result = self.mysql_dao.update(query, data)
        return result

    # UPDATE RUNTIME
    def update_runtime(self, runtime_id: int, request: RuntimeV2Request):
        query = V2_UPDATE_RUNTIME
        data = {
            "id": runtime_id,
            "class": request.class_,
            "type": request.type,
            "image": request.image,
            "reference_link": request.reference_link,
            "last_updated_by": request.user,
            "spark_connect": request.spark_connect,
            "spark_auto_init": request.spark_auto_init,
        }
        self.mysql_dao.update(query, data)

    def set_as_default_runtime(self, runtime_id: int, runtime_class: str) -> None:
        query = V2_SET_DEFAULT_RUNTIME
        data = {"runtime_id": runtime_id, "class": runtime_class}
        self.mysql_dao.create(query, data)

    # CREATE RUNTIME
    def update_soft_deleted_runtime(self, runtime_id: int, request: RuntimeV2Request):
        query = V2_UPDATE_SOFT_DELETED_RUNTIME
        data = {
            "id": runtime_id,
            "class": request.class_,
            "type": request.type,
            "image": request.image,
            "reference_link": request.reference_link,
            "created_by": request.user,
            "last_updated_by": request.user,
            "spark_connect": request.spark_connect,
            "spark_auto_init": request.spark_auto_init,
        }
        self.mysql_dao.update(query, data)

    def create_runtime(self, request: RuntimeV2Request):
        query = V2_CREATE_RUNTIME
        data = {
            "runtime": request.runtime,
            "class": request.class_,
            "type": request.type,
            "image": request.image,
            "reference_link": request.reference_link,
            "created_by": request.user,
            "last_updated_by": request.user,
            "spark_connect": request.spark_connect,
            "spark_auto_init": request.spark_auto_init,
        }
        runtime = self.mysql_dao.create(query, data)
        return runtime[0]
