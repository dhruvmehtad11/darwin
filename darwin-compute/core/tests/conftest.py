import os

import pytest

from compute_app_layer.models.runtime_v2 import (
    RuntimeV2Request,
    RuntimeComponent,
    ComponentNameEnum,
    RuntimeClassEnum,
    RuntimeTypeEnum,
    GetRuntimesRequest,
    RuntimeV2Details,
)
from compute_core.compute import Compute
from compute_core.dto.library_dto import LibraryDTO, LibraryStatus, LibraryType, LibrarySource
from compute_core.dto.remote_command_dto import RemoteCommandDto, RemoteCommandStatus, RemoteCommandTarget
from compute_core.dto.request.es_compute_cluster_definition import ESComputeDefinition
from compute_model.advance_config import AdvanceConfig
from compute_model.compute_cluster import ComputeClusterDefinition
from compute_model.head_node import HeadNode
from compute_model.worker_group import WorkerGroup

env = os.getenv("ENV", "local")


@pytest.fixture
def library_dto():
    return LibraryDTO(
        id=1,
        cluster_id="cluster_123",
        name="example_library",
        version="1.0.0",
        status=LibraryStatus.CREATED,
        type=LibraryType.JAR,
        source=LibrarySource.PYPI,
        path="/path/to/library",
        metadata={"key": "value"},
        execution_id="execution_123",
    )


@pytest.fixture
def basic_compute_cluster_def():
    return ComputeClusterDefinition(
        name="test-cluster",
        tags=["test"],
        labels={"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"},
        runtime="Ray2.37.0-Py310-Spark3.5.1-CPU",
        head_node=HeadNode({"cores": 2, "memory": 4}),
        worker_group=[WorkerGroup({"cores": 2, "memory": 4}, 1, 2)],
        user="darwin.compute@dream11.com",
        cloud_env="eks-0",
    )


@pytest.fixture
def es_compute_def(basic_compute_cluster_def):
    return ESComputeDefinition.from_dict(basic_compute_cluster_def.to_dict())


@pytest.fixture
def compute_cluster_def_with_ondemand_head_node():
    return ComputeClusterDefinition(
        name="test-cluster",
        tags=["test"],
        labels={"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"},
        runtime="Ray2.37.0-Py310-Spark3.5.1-CPU",
        head_node=HeadNode({"cores": 2, "memory": 4, "node_capacity_type": "ondemand"}),
        worker_group=[WorkerGroup({"cores": 2, "memory": 4}, 1, 2)],
        user="darwin.compute@dream11.com",
        cloud_env="eks-0",
    )


@pytest.fixture
def compute_cluster_def_with_ondemand_worker_group():
    return ComputeClusterDefinition(
        name="test-cluster",
        tags=["test"],
        labels={"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"},
        runtime="Ray2.37.0-Py310-Spark3.5.1-CPU",
        head_node=HeadNode({"cores": 4, "memory": 8}),
        worker_group=[
            WorkerGroup({"cores": 4, "memory": 8, "node_capacity_type": "ondemand"}, 1, 2),
            WorkerGroup({"cores": 4, "memory": 8, "node_capacity_type": "spot"}, 1, 2),
        ],
        user="darwin.compute@dream11.com",
        cloud_env="eks-0",
        cluster_id="id-test",
    )


@pytest.fixture
def compute_cluster_id(basic_compute_cluster_def):
    compute = Compute(env)
    cluster_id = compute.create_cluster(basic_compute_cluster_def)["cluster_id"]
    yield cluster_id
    compute.delete_cluster(cluster_id)


@pytest.fixture
def compute_cluster_request():
    return ComputeClusterDefinition(
        name="test",
        tags=["tag1", "tag2"],
        labels={"project": "darwin", "service": "darwin", "squad": "data-science", "environment": "test"},
        runtime="test",
        head_node=HeadNode(node={"cores": 1, "memory": 1}),
        worker_group=[
            WorkerGroup(
                node={"cores": 1, "memory": 1, "disk": None, "node_capacity_type": "ondemand", "max_pods": 2},
                min_pods=1,
                max_pods=2,
                node_type="general",
            ),
            WorkerGroup(
                node={"cores": 2, "memory": 4, "disk": None, "node_capacity_type": "spot"},
                min_pods=1,
                max_pods=2,
                node_type="memory",
            ),
        ],
        terminate_after_minutes=60,
        advance_config=AdvanceConfig(
            env_variables="TEST_ENV=test\nTEST_ENV2=test2\n", init_script=["pip3 install cli"], instance_role="ray"
        ),
        user="darwin@test.com",
        created_on="",
        cluster_id="test",
        cloud_env="eks-0",
        is_job_cluster=False,
    )


@pytest.fixture
def all_cluster_events():
    return [
        "POD_KILLING",
        "POD_UNHEALTHY",
        "POD_FAILED",
        "POD_FAILED_SCHEDULING",
        "POD_SCHEDULED",
        "POD_STARTED",
        "POD_CREATED",
        "POD_EVICTED",
        "POD_BACKOFF",
        "CLUSTER_CREATION_REQUEST_RECEIVED",
        "CLUSTER_CREATED",
        "CLUSTER_CREATION_FAILED",
        "CLUSTER_DELETION_REQUEST_RECEIVED",
        "CLUSTER_DELETED",
        "CLUSTER_DELETION_FAILED",
        "CLUSTER_UPDATION_REQUEST_RECEIVED",
        "CLUSTER_UPDATED",
        "CLUSTER_UPDATION_FAILED",
        "CLUSTER_START_REQUEST_RECEIVED",
        "CLUSTER_START_FAILED",
        "CLUSTER_STOP_REQUEST_RECEIVED",
        "CLUSTER_STOPPED",
        "CLUSTER_STOP_FAILED",
        "CLUSTER_RESTART_REQUEST_RECEIVED",
        "CLUSTER_RESTART_FAILED",
        "HEAD_NODE_UP",
        "WORKER_NODES_UP",
        "JUPYTER_UP",
        "CLUSTER_READY",
        "CLUSTER_TIMEOUT",
        "AUTO_TERMINATED",
        "INIT_SCRIPT_EXECUTION_STARTED",
        "INIT_SCRIPT_EXECUTION_SUCCESSFUL",
        "INIT_SCRIPT_EXECUTION_FAILED",
        "INSTANCE_LAUNCHED",
        "INSTANCE_PENDING",
        "INSTANCE_RUNNING",
        "INSTANCE_STOPPING",
        "INSTANCE_STOPPED",
        "INSTANCE_SHUTTING_DOWN",
        "INSTANCE_TERMINATED",
        "SPOT_INTERRUPTION",
    ]


@pytest.fixture
def remote_commands():
    return [
        RemoteCommandDto(
            execution_id="123",
            command="echo 'Head' ; echo 'Ray'",
            timeout=60,
            target=RemoteCommandTarget.head,
            status=RemoteCommandStatus.CREATED,
        ),
        RemoteCommandDto(
            execution_id="124",
            command="echo 'Worker'",
            timeout=60,
            target=RemoteCommandTarget.worker,
            status=RemoteCommandStatus.CREATED,
        ),
        RemoteCommandDto(
            execution_id="125",
            command="echo 'Hello World'",
            timeout=60,
            target=RemoteCommandTarget.cluster,
            status=RemoteCommandStatus.CREATED,
        ),
    ]


@pytest.fixture
def runtime_v2_request_def():
    params = {
        "runtime": "test-runtime",
        "class": "CPU",
        "type": "Ray and Spark",
        "image": "test-image",
        "reference_link": "test-reference-link",
        "components": [
            {
                "name": ComponentNameEnum.RAY,
                "version": "2.37",
            },
            {
                "name": ComponentNameEnum.SPARK,
                "version": "3.5.0",
            },
            {
                "name": ComponentNameEnum.PYTHON,
                "version": "3.10",
            },
        ],
        "user": "test-user",
        "set_as_default": False,
        "spark_connect": False,
        "spark_auto_init": False,
    }
    return RuntimeV2Request(**params)


@pytest.fixture
def runtime_v2_details_def():
    params = {
        "id": 1,
        "runtime": "test-runtime",
        "class": "CPU",
        "type": "Ray and Spark",
        "image": "test-image",
        "reference_link": "",
        "components": None,
        "created_by": "test-user",
        "created_at": "2025-01-01T00:00:00Z",
        "last_updated_by": "test-user",
        "last_updated_at": "2025-01-01T00:00:00Z",
        "is_deleted": False,
        "spark_connect": False,
        "spark_auto_init": False,
    }
    return RuntimeV2Details(**params)


@pytest.fixture
def runtime_component_def():
    components = [
        RuntimeComponent(name=ComponentNameEnum.RAY, version="2.37"),
        RuntimeComponent(name=ComponentNameEnum.SPARK, version="3.5.0"),
        RuntimeComponent(name=ComponentNameEnum.PYTHON, version="3.10"),
    ]
    return components


@pytest.fixture
def get_runtime_request_def():
    params = {
        "search_query": "",
        "offset": 0,
        "page_size": 5,
        "class": RuntimeClassEnum.CPU,
        "type": RuntimeTypeEnum.RAY_AND_SPARK,
        "is_deleted": False,
    }
    return GetRuntimesRequest(**params)
