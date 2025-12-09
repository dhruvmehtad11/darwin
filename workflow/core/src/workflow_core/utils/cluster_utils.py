import json
import logging
import time
from enum import Enum

import requests
# Changed from compute_sdk to darwin-compute: The package name 'compute_sdk' doesn't exist.
# The actual package is 'darwin-compute' (importable as 'darwin_compute') from darwin-compute/sdk.
# This fixes the "No module named 'compute_sdk'" error in Airflow plugins.
from darwin_compute.compute import ComputeCluster

from workflow_core.constants.configs import Config
from workflow_core.constants.constants import ENV_TYPE
from workflow_core.entity.cluster_entities import ClusterData
from workflow_core.error.errors import ClusterCreationFailed
from workflow_core.utils.logging_util import LoggingUtil

LOGGER = LoggingUtil().get_logger()


class ClusterStatus(str, Enum):
    """An enumeration for describing the status of a cluster."""
    ACTIVE = "active"
    INACTIVE = "inactive"
    CREATING = "creating"

    def __str__(self) -> str:
        return f"{self.value}"

    def created(self) -> bool:
        return self.value in {"active"}


class ClusterUtils:
    """
    Utility class to use compute SDK without compute dependencies.
    This is mainly created to avoid circular dependencies between compute and darwin-deployer.
    In all the other cases, we can directly use compute SDK

    Usage:
        from darwin_deployer.cluster_utils import ClusterUtils

        sdk = ClusterUtils("prod")
    """

    def __init__(self, env: ENV_TYPE, email: str = None):
        """
        :param env: String representing the environment or domain.
                    Valid values for env are ["prod", "stag", "test", "local"]
        """
        self.env = env
        self.compute_sdk = ComputeCluster(env)
        self._config = Config(env)
        self.AIRFLOW_URL = self._config.get_airflow_url
        self.compute_url = self._config.get_compute_app_layer

    def get_cluster_details(self, cluster_id: str):
        """

        :param cluster_id: Cluster identification of the cluster which needs to be updated
        :return: Returns the status of the request and cluster_id
        """
        resp = self.compute_sdk.get_details(cluster_id)
        return resp

    def get_cluster_status(self, cluster_id: str):
        resp = self.compute_sdk.get_details(cluster_id)
        return resp['data']['status']

    def get_num_nodes(self, cluster_id: str):
        resp = self.compute_sdk.get_details(cluster_id)
        total_nodes = 0
        logging.debug(f'get_pods_status response {cluster_id}: {resp}')
        worker_groups = resp['data']['worker_node_configs']
        for group in worker_groups:
            total_nodes += group['min_pods']

        return total_nodes + 1

    def create_cluster_with_yaml(self, yaml_path: str):
        resp = self.compute_sdk.create_with_yaml(yaml_path)
        id = resp['data']['cluster_id']
        return id

    def get_pods_status(self, url: str):
        """

        :param url : url
        :return: Return the status of pods
        """
        response = requests.get(url=url)
        logging.debug(f'get_pods_status response {url}: {response}')
        return response.json()['data']['Resources']

    def get_resource_status_count(self, status, resources):
        """

        :param status:
        :param resources:
        :return:
        """
        count = 0
        for resource in resources:
            if resource['Status'] == status:
                count += 1
        return count

    def restart_cluster(self, cluster_id: str):
        """

        :param cluster_id: Cluster identification of the cluster which needs to be updated
        :return: Returns the status of the request and cluster_id
        """
        resp = self.compute_sdk.restart(cluster_id)
        return json.loads(resp.text)

    def start_cluster(self, cluster_id: str):
        """

        :param cluster_id: Cluster identification of the cluster which needs to be updated
        :return: Returns the status of the request and cluster_id
        """
        resp = self.compute_sdk.start(cluster_id)
        return resp

    def stop_cluster(self, cluster_id: str):
        """

        :param cluster_id: Cluster identification of the cluster which needs to be updated
        :return: Returns the status of the request and cluster_id
        """
        resp = self.compute_sdk.stop(cluster_id)
        return resp

    def get_ray_dashboard_url(self, cluster_id: str):
        """
        :param cluster_id: Cluster identification of the cluster which needs to be updated
        :return: Returns the ray dashboard url
        """
        # Add retry logic
        internal_dashboards_url = self.compute_url + "/" + cluster_id + "/dashboards?internal=True"
        retries = 0
        while retries < 3:
            try:
                response = requests.request("GET", internal_dashboards_url).json()
                ray_dashboard_url = response["data"]["ray_dashboard_url"]
                return ray_dashboard_url[:-1]
            except Exception as e:
                LOGGER.error(f"Error in getting ray dashboard url. Retrying. Error: {str(e)}")
                retries += 1
                time.sleep(5)

    def start_cluster_and_wait(self, cluster_id: str, num_pods_active: int = 1,
                               time_out_in_sec_for_cluster: int = 3600):
        """

        :param num_pods_active: Number of pods which should be active
        :param cluster_id: Cluster identification of the cluster which needs to be updated
        :param time_out_in_sec_for_cluster: Maximum time for which you can wait for cluster to come up
        :return: Returns the status of the request and cluster_id along with jupyter and dashboard links
        """
        ray_dashboard_url = ""
        start = time.time()
        status = self.get_cluster_status(cluster_id)

        if status == ClusterStatus.ACTIVE:
            ray_dashboard_url = self.get_ray_dashboard_url(cluster_id)
            LOGGER.info(f"Cluster {cluster_id} is already up. Ray dashboard URL: {ray_dashboard_url}")
            return ClusterStatus.ACTIVE, ray_dashboard_url

        if status == ClusterStatus.INACTIVE:
            LOGGER.info(f"Cluster {cluster_id} is inactive. starting the cluster")
            start_response = self.start_cluster(cluster_id)
            LOGGER.info(start_response)
            if start_response['status'] == 'ERROR':
                msg = start_response['data']
                LOGGER.info(
                    f'Failed to restart the cluster for id {cluster_id}. Error message is {msg}', )
                return status, msg

        while time.time() - start <= time_out_in_sec_for_cluster:
            cm_status = self.get_cluster_status(cluster_id)
            LOGGER.info(f'Current Cluster Status is {cm_status}')
            if cm_status == ClusterStatus.ACTIVE:
                status = ClusterStatus.ACTIVE
                LOGGER.info(f'Current Cluster Status is {status}')
                ray_dashboard_url = self.get_ray_dashboard_url(cluster_id)
                break
            elif cm_status == ClusterStatus.INACTIVE:
                raise Exception("Cluster Creation Failed")
            else:
                time.sleep(10)

        if not status.created():
            LOGGER.info(
                f'Spent {time_out_in_sec_for_cluster} in waiting but the the cluster is still not up')
            LOGGER.info(f'Aborting the run')
            stop_response = self.stop_cluster(cluster_id)
            if stop_response['status'] == 'ERROR':
                msg = stop_response['data']
                LOGGER.info(
                    f'Failed to stop the cluster for id {cluster_id}. Error message is {msg}', )
                return status, msg
            raise Exception("Cluster Creation Timed Out")
        LOGGER.info(
            f'Total time taken for the cluster to come up is {time.time() - start} sec')
        return status, ray_dashboard_url

    def shutdown_cluster(self, cluster_id: str, wait_time_for_shutdown_in_sec):
        """

        :param cluster_id: Cluster identification of the cluster which needs to be shut down
        :param wait_time_for_shutdown_in_sec: Wait time required to shut down cluster
        :return: Returns the status of the request and cluster_id
        """
        try:
            start = time.time()
            while time.time() - start <= wait_time_for_shutdown_in_sec:
                time.sleep(12)
                LOGGER.info(
                    f'Will be shutting down cluster in {wait_time_for_shutdown_in_sec - (time.time() - start)} sec')
            LOGGER.info(f'Shutting down cluster {cluster_id}')
            return self.compute_sdk.stop(cluster_id)
        except Exception as e:
            raise e

    def delete_cluster(self, cluster_id: str):
        """

        :param cluster_id: Cluster identification of the cluster which needs to be deleted
        :return: Returns the status of the request and cluster_id
        """
        resp = self.compute_sdk.delete(cluster_id)
        return resp

    def create_cluster_from_job_definition(self, job_cluster_definition_id: str, user_email: str, try_number: int = 1):
        """
        :param job_cluster_definition_id: Job cluster definition id
        :param user_email: User email
        :param try_number: Retry number
        :return: Returns the cluster id
        """
        if not user_email:
            user_email = "sdk"
        app_layer_url = self._config.get_app_layer
        compute_app_layer = self._config.get_compute_app_layer
        Workflow_URL = app_layer_url
        url = f"{Workflow_URL}/job-cluster-definitions/{job_cluster_definition_id}"
        LOGGER.info("Creating job cluster from definition")
        response = requests.request("GET", url)
        if not 200 <= response.status_code < 300:
            raise Exception(f"Error in fetching job cluster definition. Error is {response.text}")
        job_cluster_definition = response.json()['data']
        job_cluster_definition['is_job_cluster'] = True
        job_cluster_definition['user'] = user_email
        job_cluster_definition['advance_config']['environment_variables'] = \
            job_cluster_definition['advance_config']['environment_variables'] + f"\nWORKFLOW_RETRY_NUMBER={try_number}\n"
        compute_resp = requests.request("POST", compute_app_layer, data=json.dumps(job_cluster_definition))
        if not 200 <= compute_resp.status_code < 300:
            raise ClusterCreationFailed(f"Error in creating job cluster. Error is {compute_resp.text}")
        else:
            if compute_resp.json()['status'] == 'ERROR':
                raise ClusterCreationFailed(f"Error in creating job cluster. Error is {compute_resp.json()['data']}")
        cluster_id = compute_resp.json()['data']['cluster_id']

        return cluster_id

    def create_cluster_from_job_definition_v2(self, job_cluster_definition_id: str, user_email: str):
        """
        :param job_cluster_definition_id: Job cluster definition id
        :param user_email: User email
        :return: Returns the cluster id
        """
        if not user_email:
            user_email = "sdk"
        app_layer_url = self._config.get_app_layer
        Workflow_URL = app_layer_url
        url = f"{Workflow_URL}/job-cluster-definitions/{job_cluster_definition_id}"
        LOGGER.info("Creating job cluster from definition")
        response = requests.request("GET", url)
        if not 200 <= response.status_code < 300:
            raise Exception(f"Error in fetching job cluster definition. Error is {response.text}")
        job_cluster_definition = response.json()['data']
        job_cluster_definition['inactive_time'] = -1
        job_cluster_definition['auto_termination_policies']=[]
        job_cluster_definition['user'] = user_email
        compute_resp = requests.request("POST", self.compute_url, data=json.dumps(job_cluster_definition))
        if not 200 <= compute_resp.status_code < 300:
            raise ClusterCreationFailed(f"Error in creating job cluster. Error is {compute_resp.text}")
        else:
            if compute_resp.json()['status'] == 'ERROR':
                raise ClusterCreationFailed(f"Error in creating job cluster. Error is {compute_resp.json()['data']}")
        cluster_id = compute_resp.json()['data']['cluster_id']
        return cluster_id

    def get_job_cluster_definition(self, job_cluster_definition_id: str):
        """
        :param job_cluster_definition_id: Job cluster definition id
        :return: Returns the job cluster definition
        """
        app_layer_url = self._config.get_app_layer
        Workflow_URL = app_layer_url
        url = f"{Workflow_URL}/job-cluster-definitions/{job_cluster_definition_id}"
        LOGGER.info("Fetching job cluster definition")
        response = requests.request("GET", url)
        if not 200 <= response.status_code < 300:
            raise Exception(f"Error in fetching job cluster definition. Error is {response.text}")
        return response.json()

    def get_cluster_definition(self, cluster_id: str, cluster_type: str):
        """
        Gets the cluster definition based on the cluster ID and type.
        """
        if cluster_type == "job":
            cluster_data_dict = self.get_job_cluster_definition(cluster_id)['data']
            if cluster_data_dict['advance_config']['ray_params']:
                cluster_data_dict['advance_config']['ray_start_params'] = cluster_data_dict['advance_config'][
                    'ray_params']
                cluster_data_dict['advance_config']['ray_start_params']['object_store_memory_perc'] = \
                    cluster_data_dict['advance_config']['ray_params']['object_store_memory']
                del cluster_data_dict['advance_config']['ray_params']
                del cluster_data_dict['advance_config']['ray_start_params']['object_store_memory']
        else:
            cluster_data_dict = self.compute_sdk.get_details(cluster_id)['data']
            cluster_data_dict['cluster_name'] = cluster_data_dict['name']
            cluster_data_dict['head_node_config']['cores'] = cluster_data_dict['head_node_config']['head_node_cores']
            cluster_data_dict['head_node_config']['memory'] = cluster_data_dict['head_node_config']['head_node_memory']
            for worker_config in cluster_data_dict['worker_node_configs']:
                worker_config['cores_per_pods'] = worker_config.pop('cores')
                worker_config['memory_per_pods'] = worker_config.pop('memory')

        # Convert the raw data dictionary into a ClusterData instance
        cluster_data = ClusterData(**cluster_data_dict)

        # Use the convert method to get the ClusterDict
        return cluster_data.convert()
