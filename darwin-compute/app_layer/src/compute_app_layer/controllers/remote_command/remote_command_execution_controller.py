from compute_app_layer.models.remote_command.remote_command_request import (
    RemoteCommandRequest,
    PodCommandExecutionStatusReportRequest,
    AddRemoteCommandsRequest,
)
from compute_app_layer.utils.response_handlers import ResponseHandler
from compute_app_layer.utils.response_util import Response
from compute_core.remote_command import RemoteCommand


async def add_remote_commands_to_cluster_controller(
    cluster_id: str, request: AddRemoteCommandsRequest, remote_command: RemoteCommand
):
    with ResponseHandler() as handler:
        commands = [cmd.convert() for cmd in request.remote_commands]
        resp = remote_command.add_to_cluster(cluster_id, commands)
        handler.response = Response.success_response("Remote Command Added Successfully", resp)

    return handler.response


async def execute_command_on_cluster_controller(
    cluster_id: str, request: RemoteCommandRequest, remote_command: RemoteCommand
):
    with ResponseHandler() as handler:
        resp = remote_command.execute_on_cluster(cluster_id, request.convert())
        handler.response = Response.accepted_response("Command Execution Started Successfully", resp)

    return handler.response


async def delete_remote_command_from_cluster_controller(
    cluster_id: str, execution_id: str, remote_command: RemoteCommand
):
    with ResponseHandler() as handler:
        resp = remote_command.delete_from_cluster(cluster_id, [execution_id])
        data = {"execution_id": resp["execution_ids"][0]}
        handler.response = Response.success_response("Remote Command Deleted Successfully", data)

    return handler.response


async def get_command_execution_status_controller(cluster_id: str, execution_id: str, remote_command: RemoteCommand):
    with ResponseHandler() as handler:
        resp = remote_command.get_execution_status(execution_id)
        handler.response = Response.success_response("Command Execution Status Fetched Successfully", resp)

    return handler.response


async def get_cluster_remote_commands_controller(cluster_id: str, remote_command: RemoteCommand):
    with ResponseHandler() as handler:
        remote_commands = remote_command.get_all_of_cluster(cluster_id)
        resp = {"remote_commands": remote_commands}
        handler.response = Response.success_response("All Remote Commands Fetched Successfully", resp)

    return handler.response


async def set_pod_command_execution_status_controller(
    request: PodCommandExecutionStatusReportRequest, remote_command: RemoteCommand
):
    with ResponseHandler() as handler:
        resp = remote_command.set_pod_status(request)
        handler.response = Response.success_response("Pod Command Execution Status Set Successfully", resp)

    return handler.response
