from loguru import logger

from compute_core.compute import Compute
from compute_core.constant.config import Config
from compute_core.dto.cluster_resource_dto import ClusterResourceDTO
from compute_core.dto.remote_command_dto import PodCommandExecutionStatusDto, RemoteCommandTarget, RemoteCommandStatus
from compute_core.dto.request.es_compute_cluster_definition import ESComputeDefinition
from compute_script.constant.constants import RemoteCommandErrorCode
from compute_script.dao.sql_dao import ScriptMySQLDao
from compute_script.dto.remote_command_dto import RunningRemoteCommandDto, RemoteCommandExecutionUpdateStatusDTO


def get_cluster_running_pods(compute: Compute, cluster_id: str) -> list[ClusterResourceDTO]:
    """
    Get running pods of the cluster from k8s
    """
    cluster_details: ESComputeDefinition = compute.get_cluster(cluster_id)
    runtime = cluster_details.runtime

    namespace = compute.runtime_dao.get_runtime_namespace(runtime)
    kube_cluster = compute.get_kube_cluster(cluster_id)

    k8s_resources = compute.dcm.cluster_status(cluster_id, namespace, kube_cluster)
    return k8s_resources


def filter_pods(
    remote_command: RunningRemoteCommandDto, k8s_resources: list[ClusterResourceDTO]
) -> list[ClusterResourceDTO]:
    """
    Filter pods to check on basis of remote command target
    """
    if remote_command.target == RemoteCommandTarget.head:
        return [resource for resource in k8s_resources if "head" in resource.Name]
    elif remote_command.target == RemoteCommandTarget.worker:
        return [resource for resource in k8s_resources if "worker" in resource.Name]
    return k8s_resources


def get_updated_status_for_remote_command(
    remote_command: RunningRemoteCommandDto,
    k8s_resources: list[ClusterResourceDTO],
    pod_execution_statuses: list[PodCommandExecutionStatusDto],
    config: Config,
):
    """
    Compares resources on k8s and DB and returns the updated status of the remote command
    """
    updated_status = remote_command.status
    error_logs_path = None
    error_code = None
    success_count = 0

    if len(k8s_resources) == 0:
        return RemoteCommandExecutionUpdateStatusDTO(
            status=updated_status,
            error_logs_path=error_logs_path,
            error_code=RemoteCommandErrorCode.REMOTE_COMMAND_FAILED.value,
        )

    for resource in k8s_resources:
        pod = next((pod for pod in pod_execution_statuses if pod.pod_name == resource.Name), None)

        if not pod:
            return RemoteCommandExecutionUpdateStatusDTO(
                status=updated_status, error_logs_path=error_logs_path, error_code=error_code
            )

        if pod.status == RemoteCommandStatus.SUCCESS.value:
            success_count += 1
        elif pod.status == RemoteCommandStatus.FAILED.value:
            updated_status = RemoteCommandStatus.FAILED
            error_logs_path = f"s3://{config.remote_command_config['logs_s3_bucket']}/{config.remote_command_config['logs_s3_key']}/{remote_command.execution_id}/{pod.pod_name}/remote-command.log"
            error_code = RemoteCommandErrorCode.REMOTE_COMMAND_FAILED.value
            break

    if success_count == len(k8s_resources):
        updated_status = RemoteCommandStatus.SUCCESS

    return RemoteCommandExecutionUpdateStatusDTO(
        status=updated_status, error_logs_path=error_logs_path, error_code=error_code
    )


def remote_command_execution_status_update(compute: Compute, dao: ScriptMySQLDao, config: Config):
    remote_commands_running = dao.get_running_remote_commands()
    logger.debug(f"Running remote commands: {remote_commands_running}")

    for remote_command in remote_commands_running:
        cluster_run_id = dao.get_active_cluster_run_id(remote_command.cluster_id)
        logger.debug(f"Cluster Run ID: {cluster_run_id}")

        k8s_resources = get_cluster_running_pods(compute, remote_command.cluster_id)
        logger.debug(f"K8s Resources before filtering: {k8s_resources}")

        k8s_resources = filter_pods(remote_command, k8s_resources)
        logger.debug(f"K8s Resources after filtering: {k8s_resources}")

        pod_execution_statuses = dao.get_pods_command_execution_status(cluster_run_id, remote_command.execution_id)
        logger.debug(f"Pod Execution Statuses: {pod_execution_statuses}")

        remote_command_execution_status_updated = get_updated_status_for_remote_command(
            remote_command, k8s_resources, pod_execution_statuses, config
        )
        logger.debug(f"Remote Command Execution Status Updated: {remote_command_execution_status_updated}")

        dao.update_remote_command_execution_status(
            remote_command.execution_id,
            remote_command_execution_status_updated.status.value,
            remote_command_execution_status_updated.error_logs_path,
            remote_command_execution_status_updated.error_code,
        )
        logger.debug(f"Cluster Status of {remote_command.execution_id} is {remote_command.status}")
