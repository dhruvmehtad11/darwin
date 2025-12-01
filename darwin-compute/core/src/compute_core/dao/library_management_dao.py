import json

from loguru import logger

from compute_app_layer.models.request.library import SearchLibraryRequest
from compute_core.dao.mysql_dao import MySQLDao, CustomTransaction
from compute_core.dao.queries.sql_queries import (
    GET_LIBRARIES_FOR_CLUSTER,
    INSERT_LIBRARY,
    GET_LIBRARY,
    UPDATE_LIBRARY_STATUS,
    GET_LIBRARIES_FROM_ID,
    GET_LIBRARY_WITH_STATUS_AND_CLUSTER_ID,
    UPDATE_STATUS_OF_LIBRARY_HAVING_ID,
    DELETE_LIBRARY_WITH_ID,
    UPDATE_CLUSTER_LIBRARY_STATUS,
    GET_LIBRARIES_COUNT_FOR_CLUSTER,
    GET_CLUSTER_LIBRARIES,
    UPDATE_LIBRARY_STATUS_AND_EXECUTION_ID,
    UPDATE_RUNNING_LIBRARIES_TO_CREATED,
)
from compute_core.dto.exceptions import LibraryNotFoundError
from compute_core.dto.library_dto import LibraryDTO, LibraryStatus
from compute_core.util.utils import add_list_to_query


class LibraryDao(MySQLDao):
    def __init__(self, env: str = None):
        super().__init__(env)

    def search(self, request: SearchLibraryRequest) -> [LibraryDTO]:
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            logger.debug(
                f"Executing db query to fetch libraries for cluster: {request.cluster_id}, request: {request.dict()}"
            )
            query = GET_LIBRARIES_FOR_CLUSTER
            data = request.dict()
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.fetchall()
            logger.debug(f"Libraries fetched successfully for cluster: {request.cluster_id}")
            for r in resp:
                if "metadata" in r and r["metadata"]:
                    r["metadata"] = json.loads(r["metadata"])
            return [LibraryDTO(**r) for r in resp]

    def search_count(self, request: SearchLibraryRequest) -> int:
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            logger.debug(
                f"Executing db query to fetch libraries count for cluster: {request.cluster_id}, request: {request.dict()}"
            )
            query = GET_LIBRARIES_COUNT_FOR_CLUSTER
            data = request.dict()
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.fetchone()
            logger.debug(f"Libraries fetched successfully for cluster: {request.cluster_id} {resp}")
            return int(resp["library_count"])

    def add_library(self, libraries: list[LibraryDTO]) -> list[LibraryDTO]:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            logger.debug(f"Inserting libraries in db: {libraries}")
            query = INSERT_LIBRARY
            for library in libraries:
                data = library.to_dict_with_enum_values()
                if "metadata" in data and data["metadata"]:
                    data["metadata"] = json.dumps(data["metadata"])
                mysql_connection.execute_query(query, data)
                library.id = mysql_connection.cursor.lastrowid
            logger.debug(f"Libraries inserted successfully: {libraries}")
            return libraries

    def get_with_id(self, library_id: str) -> LibraryDTO:
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            logger.debug(f"Fetching status of library: {library_id}")
            query = GET_LIBRARY
            data = {"library_id": library_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.fetchone()
            logger.debug(f"Library status fetched successfully for library {library_id}: {resp}")
            if not resp:
                raise LibraryNotFoundError(library_id)
            if "metadata" in resp and resp["metadata"]:
                resp["metadata"] = json.loads(resp["metadata"])
            return LibraryDTO.from_dict(resp)

    def update_status(self, library_id: str, status: LibraryStatus) -> None:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            logger.debug(f"Updating status of library: {library_id}")
            query = UPDATE_LIBRARY_STATUS
            data = {"status": status.value, "library_id": library_id}
            mysql_connection.execute_query(query, data)
            logger.debug(f"Library status updated successfully for library: {library_id}")

    def get_libraries_details_from_id(self, library_ids: list[int]) -> list[LibraryDTO]:
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            logger.debug(f"get_libraries_details: {library_ids}")
            query = GET_LIBRARIES_FROM_ID
            query = add_list_to_query(query, library_ids)
            mysql_connection.execute_query(query)
            resp = mysql_connection.cursor.fetchall()
            logger.debug(f"Libraries fetched successfully with ids - {library_ids}")
            return [LibraryDTO(**r) for r in resp]

    def get_library_details_with_status_and_cluster_id(self, status: str, cluster_id: str) -> list[LibraryDTO]:
        logger.debug(f"Getting library details with status - {status} and cluster_id:- {cluster_id}")
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            query = GET_LIBRARY_WITH_STATUS_AND_CLUSTER_ID
            data = {"status": status, "cluster_id": cluster_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.fetchall()
            logger.debug(f"Libraries fetched successfully with status - {status} and cluster_id:- {cluster_id}")
            return [LibraryDTO(**r) for r in resp]

    def get_cluster_libraries(self, cluster_id: str) -> list[LibraryDTO]:
        logger.debug(f"Getting all libraries for cluster - {cluster_id}")
        with CustomTransaction(self.get_read_connection()) as mysql_connection:
            query = GET_CLUSTER_LIBRARIES
            data = {"cluster_id": cluster_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.fetchall()
            logger.debug(f"Libraries fetched successfully for cluster - {cluster_id}: {resp}")
            for r in resp:
                if "metadata" in r and r["metadata"]:
                    r["metadata"] = json.loads(r["metadata"])
            return [LibraryDTO.from_dict(r) for r in resp]

    def update_cluster_library_status(self, status: str, cluster_id: str) -> int:
        logger.debug(f"Updating status - {status} for cluster - {cluster_id}")
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = UPDATE_CLUSTER_LIBRARY_STATUS
            data = {"status": status, "cluster_id": cluster_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.rowcount
            logger.debug(f"Successfully updated libraries for cluster - {cluster_id} with status - {status}")
            return resp

    def update_running_libraries_to_created(self, cluster_id: str) -> int:
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = UPDATE_RUNNING_LIBRARIES_TO_CREATED
            data = {"cluster_id": cluster_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.rowcount
            logger.debug(f"Successfully updated running libraries for cluster - {cluster_id} to created")
            return resp

    def update_status_of_library_having_id(self, status: str, library_ids: list[int]) -> int:
        logger.debug(f"Updating status - {status} for libraries - {library_ids}")
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = UPDATE_STATUS_OF_LIBRARY_HAVING_ID
            data = {"status": status}
            query = add_list_to_query(query, library_ids)
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.rowcount
            logger.debug(f"Successfully updated libraries - {library_ids} with status - {status}")
            return resp

    def delete_libraries(self, library_ids: list[int]) -> int:
        logger.debug(f"Deleting {len(library_ids)} libraries")
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = DELETE_LIBRARY_WITH_ID
            query = add_list_to_query(query, library_ids)
            mysql_connection.execute_query(query)
            resp = mysql_connection.cursor.rowcount
            logger.info(f"Successfully deleted libraries - {library_ids}")
            return resp

    def update_status_and_execution_id(self, library_id: str, status: LibraryStatus, execution_id: str) -> int:
        logger.debug(f"Updating status - {status} and execution id - {execution_id} for library - {library_id}")
        with CustomTransaction(self.get_write_connection()) as mysql_connection:
            query = UPDATE_LIBRARY_STATUS_AND_EXECUTION_ID
            data = {"library_id": library_id, "status": status.value, "execution_id": execution_id}
            mysql_connection.execute_query(query, data)
            resp = mysql_connection.cursor.rowcount
            logger.debug(f"Successfully updated status: {status} and exec_id: {execution_id} for library: {library_id}")
            return resp
