from loguru import logger
from starlette.responses import JSONResponse

from compute_app_layer.models.request.library import (
    InstallRequest,
    SearchLibraryRequest,
    UninstallLibrariesRequest,
    InstallBatchRequest,
)
from compute_app_layer.models.response.library import LibraryDetailsResponse, LibraryListResponse
from compute_app_layer.utils.response_handlers import ResponseHandler
from compute_app_layer.utils.response_util import Response
from compute_app_layer.utils.utils import download_file_content_from_s3, download_file_content_from_workspace
from compute_core.compute import Compute
from compute_core.dto.library_dto import LibraryStatus, LibraryType, LibrarySource
from compute_core.util.package_management.package_manager import LibraryManager


async def get_libraries_controller(request: SearchLibraryRequest, lib_manager: LibraryManager) -> JSONResponse:
    try:
        logger.debug(f"Getting libraries for cluster: {request.cluster_id}, request: {request.dict()}")
        libraries = lib_manager.get_libraries(request)
        logger.debug(f"Libraries fetched successfully for cluster {request.cluster_id}: {libraries}")
        libraries_count = lib_manager.get_libraries_count(request)
        resp = LibraryListResponse(cluster_id=request.cluster_id, result_size=libraries_count, packages=libraries)
        return Response.success_response(message="Libraries fetched successfully", data=resp)
    except Exception as e:
        logger.error(f"Error searching library: {request}: {str(e)}")
        return Response.internal_server_error_response("Error fetching libraries", str(e))


async def get_library_details_controller(cluster_id: str, library_id: str, lib_manager: LibraryManager) -> JSONResponse:
    try:
        logger.debug(f"Getting details of library: {library_id} for cluster: {cluster_id}")
        library = lib_manager.get_library_details(library_id)
        logger.debug(f"Library details fetched successfully for library {library_id}: {library}")
        error_details = None
        file_content = None

        # fetch error if library status is failed
        if library.status.value == LibraryStatus.FAILED.value:
            error_details = lib_manager.get_error_details_for_library(library.execution_id)

        # fetch content if library type is txt
        if library.type == LibraryType.TXT and library.source == LibrarySource.S3:
            file_content = download_file_content_from_s3(library.path)
        elif library.type == LibraryType.TXT and library.source == LibrarySource.WORKSPACE:
            file_content = download_file_content_from_workspace(library.name)

        response = LibraryDetailsResponse(
            cluster_id=cluster_id,
            id=library.id,
            name=library.name,
            version=library.version,
            type=library.type,
            source=library.source,
            path=library.path,
            content=file_content,
            error=error_details,
            status=library.status,
        )
        return Response.success_response(message="Library details fetched successfully", data=response)
    except Exception as e:
        logger.error(f"Error fetching details of library: {library_id} for cluster: {cluster_id}: {str(e)}")
        return Response.internal_server_error_response("Error fetching library details", str(e))


async def install_library_controller(
    cluster_id: str, library: InstallRequest, lib_manager: LibraryManager, compute: Compute
) -> JSONResponse:
    try:
        logger.debug(f"Installing library: {library} for cluster: {cluster_id}")

        # Get cluster status
        cluster_status = compute.get_cluster(cluster_id=cluster_id).status

        # Install the library based on cluster status
        resp = lib_manager.add_library(cluster_id, cluster_status, [library])
        logger.debug(f"Library {library} installed successfully on cluster {cluster_id}")
        return Response.success_response("Library installed successfully", data=resp[0] if resp else None)
    except Exception as e:
        logger.error(f"Error installing library: {library} on cluster: {cluster_id}: {str(e)}")
        return Response.internal_server_error_response("Error installing library", str(e))


async def install_package_batch_controller(
    cluster_id: str, request: InstallBatchRequest, lib_manager: LibraryManager, compute: Compute
) -> JSONResponse:
    try:
        logger.debug(f"Installing libraries in batch for cluster: {cluster_id}, request: {request.dict()}")

        # Get cluster status
        cluster_status = compute.get_cluster(cluster_id=cluster_id).status

        # Install the libraries based on cluster status
        resp = lib_manager.add_library(cluster_id, cluster_status, request.packages)
        logger.debug(f"Batch library installation successful for cluster {cluster_id}")
        data = {"cluster_id": cluster_id, "packages": resp}
        return Response.success_response("Batch library installation successful", data=data)
    except Exception as e:
        logger.error(f"Error installing libraries in batch for cluster: {cluster_id}: {str(e)}")
        return Response.internal_server_error_response("Error installing libraries in batch", str(e))


async def get_library_status_controller(cluster_id: str, library_id: str, lib_manager: LibraryManager) -> JSONResponse:
    try:
        logger.debug(f"Getting status of library: {library_id} for cluster: {cluster_id}")
        status: LibraryStatus = lib_manager.get_status(cluster_id, library_id)
        resp = {"library_id": library_id, "cluster_id": cluster_id, "status": status.value}
        return Response.success_response(message="Library status fetched successfully", data=resp)
    except Exception as e:
        logger.error(f"Error fetching status of library: {library_id} for cluster: {cluster_id}: {str(e)}")
        return Response.internal_server_error_response("Error fetching library status", str(e))


async def uninstall_libraries_controller(
    cluster_id: str, request: UninstallLibrariesRequest, lib_manager: LibraryManager, compute: Compute
):
    try:
        logger.debug(f"Uninstalling libraries: {request.id} for cluster: {cluster_id}")

        # Get cluster status
        cluster_status = compute.get_cluster(cluster_id=cluster_id).status

        # Uninstall the libraries based on cluster status
        resp = lib_manager.delete_cluster_library(request.id, cluster_id, cluster_status)

        logger.debug(f"Successfully deleted {request.id} for cluster: {cluster_id}")

        return Response.success_response(
            data=resp.to_dict(encode_json=True), message="Library Uninstallation Successful"
        )
    except Exception as e:
        logger.error(f"Failed to delete libraries - {request.id} in cluster: {cluster_id} with error: {str(e)}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)


async def retry_install_library_controller(
    cluster_id: str, library_id: str, lib_manager: LibraryManager, compute: Compute
) -> JSONResponse:
    with ResponseHandler() as handler:
        logger.debug(f"Retrying library installation for library: {library_id} on cluster: {cluster_id}")

        # Get cluster status
        cluster_status = compute.get_cluster(cluster_id=cluster_id).status

        # Retry the library installation based on cluster status
        resp = lib_manager.retry_install(cluster_id, library_id, cluster_status)

        logger.debug(f"Library {library_id} installation retried successfully on cluster {cluster_id}")
        handler.response = Response.success_response(message="Library installation retried successfully", data=resp)

    return handler.response
