import json
import os
from os import environ

import aiohttp
import pytest
from wiremock.constants import Config
from wiremock.resources.mappings import Mapping, MappingRequest, MappingResponse
from wiremock.resources.mappings.resource import Mappings
from wiremock.testing.testcontainer import wiremock_container

from test.util.wiremock_utils import parse_request_body, parse_params_and_headers

pytest_plugins = ('pytest_asyncio',)

curr_dir = os.path.dirname(os.path.abspath(__file__))
with open(curr_dir + "/../resources/ExpectedTestData.json", "r") as file:
  expected_data = json.load(file)

with open(curr_dir + "/../resources/TestData.json", "r") as file:
  test_data = json.load(file)

environ["sdk.python.environment"] = "test"


@pytest.fixture(scope="module")
def init_wiremock_server():
  with wiremock_container(image=f"{environ.get('TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX', '')}wiremock/wiremock:2.35.1-1",
                          verify_ssl_certs=False, secure=False) as wm:
    Config.base_url = wm.get_url("__admin")
    environ["ofs.admin.host"] = wm.get_base_url()
    environ["ofs.server.host"] = wm.get_base_url()
    with open(curr_dir + "/../resources/OfsAdminMockData.json", "r") as mock_file:
      mocks = json.load(mock_file)
      for mock in mocks:
        endpoint = mocks[mock]["endpoint"]
        method = mocks[mock]["method"]
        status = mocks[mock]["status"]
        request_body = mocks[mock].get("requestBody", None)
        body = mocks[mock]["body"]
        body = json.dumps(body)
        params = parse_params_and_headers(mocks[mock].get("params", {}))
        headers = parse_params_and_headers(mocks[mock].get("headers", {}))
        Mappings.create_mapping(
          Mapping(
            request=MappingRequest(method=method, url_path_pattern=endpoint, query_parameters=params,
                                   headers=headers) if request_body is None else \
              MappingRequest(method=method, url_path_pattern=endpoint, query_parameters=params, headers=headers,
                             body_patterns=parse_request_body(request_body)),
            response=MappingResponse(status=status, body=body),
            persistent=False,
          )
        )
    yield wm


@pytest.mark.usefixtures("init_wiremock_server")
def test_get_entity():
  from darwin_fs.model.entity import Entity
  from darwin_fs.client import get_entity

  entity = get_entity("t10")

  expected = Entity.from_dict(expected_data.get('test_get_entity').get('entity'))
  assert type(entity) == Entity
  assert entity == expected


@pytest.mark.usefixtures("init_wiremock_server")
def test_get_entity_not_found():
  from darwin_fs.exception import SdkException
  from darwin_fs.model.error_response import ErrorResponse
  from darwin_fs.client import get_entity

  error = None
  try:
    get_entity("t15")
  except Exception as e:
    error = e

  expected = ErrorResponse.from_dict(expected_data.get('test_get_entity_not_found'))
  assert error is not None
  assert type(error) == SdkException
  assert error.error_code == expected.error.code


@pytest.mark.usefixtures("init_wiremock_server")
def test_get_feature_group_1():
  from darwin_fs.model.feature_group import FeatureGroup
  from darwin_fs.client import get_feature_group

  feature_group = get_feature_group("f20")

  expected = FeatureGroup.from_dict(expected_data.get('test_get_feature_group_1').get('featureGroup'))
  assert type(feature_group) == FeatureGroup
  assert feature_group == expected


@pytest.mark.usefixtures("init_wiremock_server")
def test_get_feature_group_2():
  from darwin_fs.model.feature_group import FeatureGroup
  from darwin_fs.client import get_feature_group

  feature_group = get_feature_group("f20", "v2")

  expected = FeatureGroup.from_dict(expected_data.get('test_get_feature_group_2').get('featureGroup'))
  assert type(feature_group) == FeatureGroup
  assert feature_group == expected


@pytest.mark.usefixtures("init_wiremock_server")
def test_get_feature_group_not_found():
  from darwin_fs.exception import SdkException
  from darwin_fs.model.error_response import ErrorResponse
  from darwin_fs.client import get_feature_group

  error = None
  try:
    get_feature_group("f10")
  except Exception as e:
    error = e

  expected = ErrorResponse.from_dict(expected_data.get('test_get_feature_group_not_found'))
  assert error is not None
  assert type(error) == SdkException
  assert error.error_code == expected.error.code


@pytest.mark.usefixtures("init_wiremock_server")
def test_get_feature_group_schema():
  from darwin_fs.model.feature_group_schema import FeatureGroupSchema
  from darwin_fs.client import get_feature_group_schema

  feature_group = get_feature_group_schema("f20", "v2")

  expected = FeatureGroupSchema.from_dict(expected_data.get('test_get_feature_group_schema').get('response'))
  assert type(feature_group) == FeatureGroupSchema
  assert feature_group == expected


@pytest.mark.usefixtures("init_wiremock_server")
def test_create_entity():
  from darwin_fs.model.create_entity_request import CreateEntityRequest
  from darwin_fs.client import create_entity

  request = CreateEntityRequest.from_dict(test_data.get('test_create_entity').get('request'))
  error = None
  try:
    create_entity(request)
  except Exception as e:
    error = e

  assert error is None


@pytest.mark.usefixtures("init_wiremock_server")
def test_create_feature_group():
  from darwin_fs.model.create_feature_group_request import CreateFeatureGroupRequest
  from darwin_fs.client import create_feature_group

  request = CreateFeatureGroupRequest.from_dict(test_data.get('test_create_feature_group').get('request'))
  error = None
  try:
    create_feature_group(request)
  except Exception as e:
    error = e

  assert error is None


@pytest.mark.usefixtures("init_wiremock_server")
def test_get_feature_group_latest_version():
  from darwin_fs.model.feature_group_version import FeatureGroupVersion
  from darwin_fs.client import get_latest_feature_group_version

  feature_group_version = get_latest_feature_group_version("f20")

  expected = FeatureGroupVersion.from_dict(expected_data.get('test_get_feature_group_latest_version').get('version'))
  assert type(feature_group_version) == FeatureGroupVersion
  assert feature_group_version == expected


@pytest.mark.usefixtures("init_wiremock_server")
def test_update_feature_group_state():
  from darwin_fs.client import update_feature_group_state
  from darwin_fs.constant.constants import State

  error = None
  try:
    update_feature_group_state(State.ARCHIVED, "f20")
  except Exception as e:
    error = e

  assert error is None


@pytest.mark.usefixtures("init_wiremock_server")
def test_read_features():
  from darwin_fs.model.read_features_request import ReadFeaturesRequest
  from darwin_fs.model.read_features_response import ReadFeaturesResponse
  from darwin_fs.client import read_features

  request = ReadFeaturesRequest.from_dict(test_data.get('test_read_features').get('request'))
  features = read_features(request)

  expected = ReadFeaturesResponse.from_dict(expected_data.get('test_read_features').get('response'))
  assert type(features) == ReadFeaturesResponse
  assert features == expected


@pytest.mark.usefixtures("init_wiremock_server")
def test_write_features_sync():
  from darwin_fs.model.write_features_request import WriteFeaturesRequest
  from darwin_fs.model.write_features_response import WriteFeaturesResponse
  from darwin_fs.client import write_features_sync

  request = WriteFeaturesRequest.from_dict(test_data.get('test_write_features_sync').get('request'))
  features = write_features_sync(request)

  expected = WriteFeaturesResponse.from_dict(expected_data.get('test_write_features_sync').get('response'))
  assert type(features) == WriteFeaturesResponse
  assert features == expected


@pytest.mark.usefixtures("init_wiremock_server")
def test_read_features_with_version():
  from darwin_fs.model.read_features_request import ReadFeaturesRequest
  from darwin_fs.model.read_features_response import ReadFeaturesResponse
  from darwin_fs.client import read_features

  request = ReadFeaturesRequest.from_dict(test_data.get('test_read_features_with_version').get('request'))
  features = read_features(request)

  expected = ReadFeaturesResponse.from_dict(expected_data.get('test_read_features_with_version').get('response'))
  assert type(features) == ReadFeaturesResponse
  assert features == expected


@pytest.mark.usefixtures("init_wiremock_server")
def test_write_features_sync_with_version():
  from darwin_fs.model.write_features_request import WriteFeaturesRequest
  from darwin_fs.model.write_features_response import WriteFeaturesResponse
  from darwin_fs.client import write_features_sync

  request = WriteFeaturesRequest.from_dict(test_data.get('test_write_features_sync_with_version').get('request'))
  features = write_features_sync(request)

  expected = WriteFeaturesResponse.from_dict(expected_data.get('test_write_features_sync_with_version').get('response'))
  assert type(features) == WriteFeaturesResponse
  assert features == expected


@pytest.mark.asyncio
@pytest.mark.usefixtures("init_wiremock_server")
async def test_write_features_async_with_version():
  from darwin_fs.model.write_features_request import WriteFeaturesRequest
  from darwin_fs.model.write_features_response import WriteFeaturesResponse
  from darwin_fs.client import write_features_async

  request = WriteFeaturesRequest.from_dict(test_data.get('test_write_features_sync_with_version').get('request'))

  async with aiohttp.ClientSession() as session:
    features = await write_features_async(session, request)

  expected = WriteFeaturesResponse.from_dict(expected_data.get('test_write_features_sync_with_version').get('response'))
  assert type(features) == WriteFeaturesResponse
  assert features == expected


@pytest.mark.asyncio
@pytest.mark.usefixtures("init_wiremock_server")
async def test_read_features_with_version():
  from darwin_fs.model.read_features_request import ReadFeaturesRequest
  from darwin_fs.model.read_features_response import ReadFeaturesResponse
  from darwin_fs.client import read_features_async

  request = ReadFeaturesRequest.from_dict(test_data.get('test_read_features_with_version').get('request'))

  async with aiohttp.ClientSession() as session:
    features = await read_features_async(session, request)

  expected = ReadFeaturesResponse.from_dict(expected_data.get('test_read_features_with_version').get('response'))
  assert type(features) == ReadFeaturesResponse
  assert features == expected
