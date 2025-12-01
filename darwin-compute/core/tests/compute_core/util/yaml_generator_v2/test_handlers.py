import importlib.resources as pkg_resource
from unittest.mock import patch

import pytest
import yaml

import compute_core.util.resources as rs
from compute_core.util.yaml_generator_v2.disk_handler import DiskHandler
from compute_core.util.yaml_generator_v2.long_running_cluster_handler import LongRunningClusterHandler
from compute_core.util.yaml_generator_v2.monitoring_handler import MonitoringHandler
from compute_core.util.yaml_generator_v2.remote_command_handler import RemoteCommandHandler
from compute_core.util.yaml_generator_v2.service_account_handler import ServiceAccountHandler
from compute_core.util.yaml_generator_v2.yaml_generator_v2 import ImageUpdateHandler


def prepare_values(compute_cluster_request):
    with pkg_resource.open_text(rs, "values.yaml") as stream:
        stream = stream.read().replace("CLUSTER_ID", compute_cluster_request.cluster_id)
        # use global variable values such that it overrides global values
        values = yaml.safe_load(stream)

    return values


@patch("compute_core.util.utils.RuntimeDao")
def test_gcp_image_update_handler(mock_image_runtime_dao, compute_cluster_request):
    mock_image_runtime_dao.return_value.get_runtime_image.return_value = "test:v1"

    values = prepare_values(compute_cluster_request)
    values.pop("additionalWorkerGroups")
    compute_cluster_request.cloud_env = "gcp"

    env = "test"

    handler = ImageUpdateHandler()
    values = handler.handle(values.copy(), compute_cluster_request, env, [])
    assert values["image"]["repository"] == "gcr.io/d11-causality/ray-images"
    assert values["image"]["tag"] == "v1"


@patch("compute_core.util.utils.RuntimeDao")
def test_image_update_handler(mock_image_runtime_dao, compute_cluster_request):
    mock_image_runtime_dao.return_value.get_runtime_image.return_value = "test:v1"

    values = prepare_values(compute_cluster_request)

    env = "test"
    handler = ImageUpdateHandler()
    values = handler.handle(values.copy(), compute_cluster_request, env, [])
    assert values["image"]["repository"] == "test"
    assert values["image"]["tag"] == "v1"


def test_resource_handler(compute_cluster_request):
    values = prepare_values(compute_cluster_request)
    from compute_core.util.yaml_generator_v2.yaml_generator_v2 import (
        ResourceUpdateHandler,
    )

    env = "test"
    handler = ResourceUpdateHandler()
    values = handler.handle(values, compute_cluster_request, env, [])
    assert values["head"]["resources"]["limits"]["cpu"] == 1
    assert values["head"]["resources"]["limits"]["memory"] == "1G"
    assert values["worker"]["resources"]["limits"]["cpu"] == 1
    assert values["worker"]["resources"]["limits"]["memory"] == "1G"


@patch("compute_core.util.utils.RuntimeV2Dao")
def test_env_variables_handler(mock_runtime_v2_dao, compute_cluster_request):
    """
    Test case for env_variables_handler
    """
    mock_runtime_v2_dao.return_value.get_runtime_by_name.return_value = {"spark_connect": 0, "spark_auto_init": 0}

    from compute_core.util.yaml_generator_v2.yaml_generator_v2 import (
        EnvVariablesUpdateHandler,
    )

    values = prepare_values(compute_cluster_request)

    env = "test"
    handler = EnvVariablesUpdateHandler()
    values = handler.handle(values, compute_cluster_request, env, [])

    assert len(values["head"]["containerEnv"]) > 0
    assert len(values["worker"]["containerEnv"]) > 0
    assert len(values["additionalWorkerGroups"]["wg1"]["containerEnv"]) > 0

    assert "TEST_ENV" in [env["name"] for env in values["common"]["containerEnv"]]
    assert "TEST_ENV2" in [env["name"] for env in values["common"]["containerEnv"]]

    assert "test" in [(env["value"] if "value" in env else "") for env in values["common"]["containerEnv"]]
    assert "test2" in [(env["value"] if "value" in env else "") for env in values["common"]["containerEnv"]]

    # Check if gcp env variables can be overridden
    compute_cluster_request.advance_config.env_variables = "AWS_ENDPOINT_URL_S3=test_aws"
    values = handler.handle(values, compute_cluster_request, env, [])
    assert "AWS_ENDPOINT_URL_S3" in [env["name"] for env in values["common"]["containerEnv"]]
    assert "test_aws" in [(env["value"] if "value" in env else "") for env in values["common"]["containerEnv"]]

    # Check if updating predefined env variables raises exception
    compute_cluster_request.advance_config.env_variables = "ENV=test"
    with pytest.raises(Exception, match="Predefined environment cannot be overridden"):
        handler.handle(values, compute_cluster_request, env, [])


def test_long_running_cluster_handler(compute_cluster_request):
    compute_cluster_request.terminate_after_minutes = -1
    values = prepare_values(compute_cluster_request)
    env = "test"

    handler = LongRunningClusterHandler()
    values = handler.handle(values, compute_cluster_request, env, [])

    assert values["head"]["nodeSelector"]["darwin.dream11.com/resource"] == f"{compute_cluster_request.cluster_id}-head"
    assert values["worker"]["nodeSelector"]["darwin.dream11.com/resource"] == compute_cluster_request.cluster_id
    assert (
        values["additionalWorkerGroups"]["wg1"]["nodeSelector"]["darwin.dream11.com/resource"]
        == compute_cluster_request.cluster_id
    )


def test_service_account_handler(compute_cluster_request):
    values = prepare_values(compute_cluster_request)

    env = "test"
    handler = ServiceAccountHandler()
    values = handler.handle(values.copy(), compute_cluster_request, env, [])

    assert values["head"]["serviceAccountName"] == compute_cluster_request.advance_config.instance_role
    assert values["worker"]["serviceAccountName"] == compute_cluster_request.advance_config.instance_role

    if "additionalWorkerGroups" in values.keys():
        for wg in values["additionalWorkerGroups"]:
            assert (
                values["additionalWorkerGroups"][wg]["serviceAccountName"]
                == compute_cluster_request.advance_config.instance_role
            )


def test_monitoring_handler(compute_cluster_request):
    values = prepare_values(compute_cluster_request)
    env = "test"
    handler = MonitoringHandler()
    values = handler.handle(values.copy(), compute_cluster_request, env, [])

    assert values["prometheus"]["replicas"] == 1
    assert (
        values["prometheus"]["remoteWrite"][0]["url"]
        == "http://darwin-thanos-receive-test.dream11-test.local:10908/api/v1/receive"
    )
    assert values["prometheus"]["nodeSelector"] == {"app": "default"}


def test_remote_command_handler(compute_cluster_request, remote_commands):
    values = prepare_values(compute_cluster_request)
    env = "test"

    handler = RemoteCommandHandler()
    values = handler.handle(values.copy(), compute_cluster_request, env, [], remote_commands)

    assert (
        values["remoteCommand"]["statusReportApi"]
        == "http://darwin-compute-test.dream11-test.local/cluster/command/pod/status"
    )
    assert values["remoteCommand"]["statusReportInterval"] == 10
    assert values["remoteCommand"]["logsS3Bucket"] == "test-bucket"
    assert values["remoteCommand"]["logsS3Key"] == "mlp/logs/remote-command"

    assert values["remoteCommand"]["commands"]["head"][0] == {
        "executionId": "123",
        "command": "echo 'Head' ; echo 'Ray'",
        "timeout": 60,
    }
    assert values["remoteCommand"]["commands"]["worker"][0] == {
        "executionId": "124",
        "command": "echo 'Worker'",
        "timeout": 60,
    }
    assert (
        values["remoteCommand"]["commands"]["head"][1]
        == values["remoteCommand"]["commands"]["worker"][1]
        == {"executionId": "125", "command": "echo 'Hello World'", "timeout": 60}
    )


def test_disk_handler(compute_cluster_request):
    compute_cluster_request.tags.append("ebs-autoscale")
    compute_cluster_request.head_node.node.node_type = "disk"
    compute_cluster_request.worker_group[0].node.node_type = "memory"
    compute_cluster_request.worker_group[1].node.node_type = "disk"

    values = prepare_values(compute_cluster_request)
    env = "test"
    handler = DiskHandler()
    values = handler.handle(values.copy(), compute_cluster_request, env, [])

    assert values["head"]["nodeSelector"]["app"] == "nvme"
    assert values["head"]["volumes"][3]["name"] == "disk-nvme"
    assert values["head"]["volumeMounts"][3]["name"] == "disk-nvme"
    assert values["head"]["volumeMounts"][3]["mountPath"] == "/tmp/disk"
    assert values["worker"]["nodeSelector"]["app"] == "ebs-autoscale"
    assert values["worker"]["volumes"][3]["name"] == "disk-ebs"
    assert values["worker"]["volumeMounts"][3]["name"] == "disk-ebs"
    assert values["worker"]["volumeMounts"][3]["mountPath"] == "/tmp/disk"
    assert values["additionalWorkerGroups"]["wg1"]["nodeSelector"]["app"] == "nvme"
    assert values["additionalWorkerGroups"]["wg1"]["volumes"][3]["name"] == "disk-nvme"
    assert values["additionalWorkerGroups"]["wg1"]["volumeMounts"][3]["name"] == "disk-nvme"
    assert values["additionalWorkerGroups"]["wg1"]["volumeMounts"][3]["mountPath"] == "/tmp/disk"


def test_worker_group_handler(compute_cluster_request):
    from compute_core.util.yaml_generator_v2.worker_node_handler import WorkerNodeUpdateHandler

    values = prepare_values(compute_cluster_request)
    env = "test"

    handler = WorkerNodeUpdateHandler()
    values = handler.handle(values.copy(), compute_cluster_request, env, [])

    assert values["worker"]["replicas"] == 1
    assert values["worker"]["nodeSelector"]["darwin.dream11.com/resource"] == "ray-cluster-all-purpose"
    assert (
        values["additionalWorkerGroups"]["wg1"]["nodeSelector"]["darwin.dream11.com/resource"]
        == "ray-cluster-all-purpose"
    )
    assert values["additionalWorkerGroups"]["wg1"]["replicas"] == 1

    assert (
        values["worker"]["affinity"]["podAffinity"]["preferredDuringSchedulingIgnoredDuringExecution"][0]["weight"]
        == 100
    )
    assert values["worker"]["affinity"]["podAffinity"]["preferredDuringSchedulingIgnoredDuringExecution"][0][
        "podAffinityTerm"
    ] == {
        "labelSelector": {
            "matchExpressions": [
                {"key": "app.kubernetes.io/instance", "operator": "In", "values": [compute_cluster_request.cluster_id]}
            ]
        },
        "topologyKey": "kubernetes.io/hostname",
    }
