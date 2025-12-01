from loguru import logger
from typeguard import typechecked

from compute_app_layer.models.jupyter_request import JupyterRequest
from compute_core.compute import Compute
from compute_core.constant.config import Config
from compute_core.constant.constants import ResourceType


@typechecked
class Jupyter:
    def start_jupyter(self, compute: Compute, config: Config, consumer_id: str):
        cloud_env = config.get_cloud_env(
            default_cloud_env=compute.get_resource_default_cloud_env(ResourceType.REMOTE_KERNEL)
        )
        kube_cluster = config.get_kube_cluster(cloud_env=cloud_env)
        result = compute.dcm.get_jupyter_client(
            namespace=config.jupyter_namespace,
            kube_cluster=kube_cluster,
            kube_cluster_key=cloud_env,
            consumer_id=consumer_id,
        )

        logger.info(f"Jupyter Client Link: {result}")
        resp = {"jupyterLink": result}
        return resp

    def restart_jupyter(self, compute: Compute, config: Config, params: JupyterRequest):
        cloud_env = config.get_cloud_env(
            default_cloud_env=compute.get_resource_default_cloud_env(ResourceType.REMOTE_KERNEL),
            cloud_env=params.cloud_env,
        )
        kube_cluster = config.get_kube_cluster(cloud_env=cloud_env)
        result = compute.dcm.restart_jupyter_client(
            jupyter_path=params.jupyter_path,
            release_name=params.release_name,
            namespace=config.jupyter_namespace,
            kube_cluster=kube_cluster,
            kube_cluster_key=cloud_env,
        )

        resp = {"jupyterLink": result}
        return resp
