from loguru import logger
import time
from os import environ

import requests
from typing import Union
import boto3

from src.constant.constants import ENV_ENVIRONMENT_VARIABLE
from src.consumers.configs.config import Config
from src.transformers.utils.ec2_client import EC2Client


ec2_client = EC2Client()

env = environ.get(ENV_ENVIRONMENT_VARIABLE, "local")

config = Config(env)


def get_base_cluster_id(name: str, namespace: str) -> Union[str, None]:
    """
    Get the base cluster ID for the given cluster name.

    :param namespace: Kubernetes namespace.
    :param name: The name of the cluster.
    :return: The base cluster ID if found, None otherwise.
    """
    if namespace == 'ray':
        name_split = name.split('-')
        return f"{name_split[0]}-{name_split[1]}"

    if namespace == 'prometheus':
        updated_name = name.removeprefix('prometheus-')
        name_split = updated_name.split('-')
        return f"{name_split[0]}-{name_split[1]}"

    return None


def get_instance_id(ip_address: str):
    url = config.get_chronos_app_layer_url + f"/ec2-details/{ip_address}"

    resp = requests.get(url)
    if resp.status_code == 200:
        return resp.json()['data']
    else:
        logger.exception(f"Failed to get instance ID from IP address: {ip_address}")
        return None



def get_instance_id_from_ip(ip_address):
    filters = [{
        'Name': 'network-interface.addresses.private-ip-address',
        'Values': [ip_address],
    }]
    result_list = ec2_client.describe_instances(filters=filters)

    if result_list:
        return result_list[0]['Instances'][0]['InstanceId']
    else:
        return None


def convert_private_dns_name_to_ip(private_dns_name: str):
    return private_dns_name.split('.')[0].replace('-', '.')[3:]