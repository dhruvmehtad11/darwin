import time

import requests
from loguru import logger

from compute_core.dto.cluster_resource_dto import ClusterResourceDTO
from compute_core.dto.request.es_compute_cluster_definition import ESComputeDefinition
from compute_model.cluster_status import ClusterStatus
from compute_script.dto.compute_cluster_dto import Node, ComputeClusterResourcesRequired


def get_compute_cluster_resources_required(cluster_details: ESComputeDefinition) -> ComputeClusterResourcesRequired:
    resources_required = ComputeClusterResourcesRequired()
    resources_required.worker_nodes = sum([wg.min_pods for wg in cluster_details.worker_group])
    resources_required.total_memory = (
        sum([wg.node.memory * wg.min_pods for wg in cluster_details.worker_group])
        + cluster_details.head_node.node.memory
    )
    return resources_required


def is_jupyter_up(
    cluster_id: str, jupyter_link: str, retries: int = 3, timeout: int = 1, backoff_factor: float = 0.5
) -> bool:
    for attempt in range(retries):
        try:
            resp = requests.get(jupyter_link, timeout=timeout)
            logger.debug(f"Jupyter.check.response for {cluster_id} [attempt {attempt + 1}]: {resp}")
            resp.raise_for_status()
            return True
        except requests.exceptions.RequestException as e:
            logger.warning(f"Jupyter.check.failed.attempt for {cluster_id} [attempt {attempt + 1}]  : {e}")
            if attempt < retries - 1:
                sleep_time = backoff_factor * (2**attempt)
                time.sleep(sleep_time)
        except Exception as e:
            logger.error(f"Jupyter.request.error for {cluster_id}: {e}")
            break
    logger.error(f"Jupyter.check. all {retries} attempts failed for {cluster_id}")
    return False


def get_compute_cluster_dto(cluster_id: str, k8s_resources: list[ClusterResourceDTO], jupyter_link: str) -> Node:
    node = Node()
    if not k8s_resources:
        return node

    node.head_node = any(resource.Status == "Running" for resource in k8s_resources if "head" in resource.Name)
    if node.head_node:
        node.jupyter = is_jupyter_up(cluster_id, jupyter_link)
    node.worker_nodes = sum(resource.Status == "Running" for resource in k8s_resources if "worker" in resource.Name)
    return node


def get_compute_cluster_current_status(
    compute_cluster_dto: Node, old_status: ClusterStatus, worker_nodes_required: int
) -> ClusterStatus:
    if compute_cluster_dto.head_node == 0:
        if old_status == ClusterStatus.creating:
            new_status = ClusterStatus.creating
        elif compute_cluster_dto.worker_nodes == 0:
            new_status = ClusterStatus.cluster_died
        else:
            new_status = ClusterStatus.head_node_died
    elif compute_cluster_dto.jupyter == 0:
        new_status = ClusterStatus.head_node_up
    elif compute_cluster_dto.worker_nodes == worker_nodes_required:
        new_status = ClusterStatus.active
    elif compute_cluster_dto.worker_nodes > worker_nodes_required:
        new_status = ClusterStatus.worker_nodes_scaled
    elif old_status in [ClusterStatus.jupyter_up, ClusterStatus.creating, ClusterStatus.head_node_up]:
        new_status = ClusterStatus.jupyter_up
    else:
        new_status = ClusterStatus.worker_nodes_died
    return new_status
