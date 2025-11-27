import time
from loguru import logger

from compute_app_layer.routers.dependency_cache import get_jupyter_dao
from compute_core.compute import Compute
from compute_core.constant.config import Config
from compute_core.constant.constants import ResourceType
from compute_core.dao.jupyter_dao import JupyterDao
from compute_model.policy_definition import PolicyDefinition
from compute_script.util.jupyter_activity import JupyterLabActivity

DEFAULT_POLICIES = [PolicyDefinition("JupyterLabActivity", {"expiry_time": 15})]


def terminate_jupyter_pod(compute: Compute, config: Config, pod: dict, jupyter_dao: JupyterDao):
    try:
        res = compute.dcm.delete_jupyter_client(
            pod["pod_name"],
            namespace=config.jupyter_namespace,
            kube_cluster=config.get_kube_cluster(
                cloud_env=config.get_cloud_env(
                    default_cloud_env=compute.get_resource_default_cloud_env(ResourceType.REMOTE_KERNEL)
                )
            ),
        )
        logger.info(f"response from jupyter delete api  {res}")

        if res["Err"] is not None:
            logger.error(f"Error in terminating Jupyter pod {pod['pod_name']}: {res['Err']}")
        else:
            logger.info(f"Deleting Jupyter pod {pod['pod_name']} from DB")
            jupyter_dao.delete_pod_details(pod["pod_name"])
            logger.info(f"Removed Jupyter pod {pod['pod_name']} from DB")
        return
    except Exception as e:
        logger.info(f"Deleting Jupyter pod {pod['pod_name']} from DB")
        jupyter_dao.delete_pod_details(pod["pod_name"])
        logger.info(f"Removed Jupyter pod {pod['pod_name']} from DB")


def auto_termination_jupyter(compute: Compute, config: Config):
    logger.info("Auto Termination Jupyter")
    try:
        jupyter_dao: JupyterDao = get_jupyter_dao()
        pod = jupyter_dao.get_unused_pods()

        unused_pod_count = jupyter_dao.get_unused_pods_count()
        logger.info(f"pod for auto_termination_jupyter {pod}")

        if not pod:
            logger.info("No unused Jupyter pods found")
            return

        logger.info(f"Unused Jupyter pods found: {pod}")

        try:
            jupyter_lab = JupyterLabActivity(expiry_time=config.jupyter_pods_max_idle_time)
            # Retrieve last element of the list which has id
            cloud_env = config.get_cloud_env(
                default_cloud_env=compute.get_resource_default_cloud_env(ResourceType.REMOTE_KERNEL)
            )
            internal_host_url = config.internal_host_url(cloud_env=cloud_env)

            jupyter_id = pod["jupyter_link"].split("/")[::-1][0]
            jupyter_internal_link = f"{internal_host_url}/{cloud_env}/{jupyter_id}"
            logger.info(f"jupyter_internal_link {jupyter_internal_link}")

            if not jupyter_lab.check_if_jupyter_up(jupyter_internal_link):
                logger.info(f"{pod['jupyter_link']} down.")
                terminate_jupyter_pod(compute, config, pod, jupyter_dao)
                return

            if unused_pod_count <= config.jupyter_pods_threshold:
                logger.info(
                    f"unused_pod_count {unused_pod_count} is less than threshold {config.jupyter_pods_threshold}"
                )
                return

            if jupyter_lab.check_if_active(jupyter_internal_link):
                logger.info(f"{pod['jupyter_link']} active.")
            else:
                logger.info(f"Terminating Jupyter pod {pod['pod_name']}")
                terminate_jupyter_pod(compute, config, pod, jupyter_dao)
        except Exception as e:
            if time.time() - pod["last_activity"] > config.jupyter_max_creation_time:
                terminate_jupyter_pod(compute, config, pod, jupyter_dao)

    except Exception as e:
        logger.exception(f"Error in auto_termination_jupyter: {e}")


def add_jupyter_pod(compute: Compute, config: Config):
    try:
        jupyter_dao: JupyterDao = get_jupyter_dao()
        unused_jupyter_pods_count = jupyter_dao.get_unused_pods_count()
        if unused_jupyter_pods_count < config.jupyter_pods_threshold:
            logger.info(f"Unused Jupyter pods count: {unused_jupyter_pods_count}")
            cloud_env = config.get_cloud_env(
                default_cloud_env=compute.get_resource_default_cloud_env(ResourceType.REMOTE_KERNEL)
            )
            kube_cluster = config.get_kube_cluster(cloud_env=cloud_env)
            logger.info(f"Jupyter job Kube cluster: {kube_cluster}")

            # Min pods to be created to maintain the threshold
            min_pods = config.jupyter_pods_threshold - unused_jupyter_pods_count
            logger.info(f"Min jupyter pods to be created: {min_pods}")
            # Adding only one pod as all workers try to create same number of pods
            result = compute.dcm.start_jupyter_client(
                namespace=config.jupyter_namespace,
                kube_cluster=kube_cluster,
                kube_cluster_key=cloud_env,
                consumer_id=None,
            )

            logger.info(f'Jupyter pod created: {result["release_name"]}')
    except Exception as e:
        logger.exception(f"Error in add_jupyter_pod: {e}")
