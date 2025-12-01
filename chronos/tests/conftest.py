import os
import pytest
from tortoise.contrib.test import finalizer, initializer
from src.dto.schema import RawEvent


@pytest.fixture(scope="session", autouse=True)
def initialize_tests(request):
    db_url = os.environ.get("TORTOISE_TEST_DB", "sqlite://:memory:")
    initializer(["models.models"], db_url=db_url, app_label="models")
    request.addfinalizer(finalizer)


@pytest.fixture
def event_data():
    data = {
        "Source": "WORKFLOW_APP_LAYER",
        "EventData": '{"entity": "WORKFLOW", "entity_id": "wf_id-n9q4usu8rkyjyoagome94mhwjs", "state": "WORKFLOW_PAUSED", "metadata": {"workflow_name": "integration_test_workflow_2e762722-fbf9-4c8b-8819-73295f0cf2d3"}, "timestamp": "2024-06-20T15:10:21", "message": "Workflow paused", "severity": "info"}',
        "EventTimestamp": "2024-06-20 09:40:22",
        "ContentType": "application/json",
        "RawEventID": 64,
        "IngestionTimestamp": "2024-06-20 09:40:22",
    }

    return RawEvent(**data)
