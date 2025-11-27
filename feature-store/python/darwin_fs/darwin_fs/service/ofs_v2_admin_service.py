import requests

from darwin_fs.config import *
from darwin_fs.constant.constants import State
from darwin_fs.model.create_entity_request import CreateEntityRequest
from darwin_fs.model.create_entity_response import CreateEntityResponse
from darwin_fs.model.create_feature_group_request import CreateFeatureGroupRequest
from darwin_fs.model.create_feature_group_response import CreateFeatureGroupResponse
from darwin_fs.model.data_response import DataResponse
from darwin_fs.model.entity import Entity
from darwin_fs.model.entity_metadata import EntityMetadata
from darwin_fs.model.feature_group import FeatureGroup
from darwin_fs.model.feature_group_metadata import FeatureGroupMetadata
from darwin_fs.model.feature_group_schema import FeatureGroupSchema
from darwin_fs.model.metadata_response import MetadataResponse
from darwin_fs.model.version_metadata import VersionMetadata
from darwin_fs.util.exception_utils import parse_api_exception


def get_feature_group_schema(feature_group_name: str, feature_group_version: None) -> FeatureGroupSchema:
  url = DARWIN_OFS_V2_ADMIN_HOST + DARWIN_OFS_V2_ADMIN_FG_SCHEMA_ENDPOINT
  params = {"name": feature_group_name}
  if feature_group_version is not None:
    params["version"] = feature_group_version

  response = requests.request("GET", url, params=params)
  if response.status_code != 200:
    parse_api_exception(response)

  return FeatureGroupSchema.from_dict(DataResponse.from_dict(response.json()).data)


def get_feature_group_metadata(feature_group_name: str, feature_group_version: None) -> FeatureGroupMetadata:
  url = DARWIN_OFS_V2_ADMIN_HOST + DARWIN_OFS_V2_ADMIN_FG_METADATA_ENDPOINT
  params = {"name": feature_group_name}
  if feature_group_version is not None:
    params["version"] = feature_group_version

  response = requests.request("GET", url, params=params)
  if response.status_code != 200:
    parse_api_exception(response)
  metadata = MetadataResponse.from_dict(DataResponse.from_dict(response.json()).data).metadata
  return FeatureGroupMetadata.from_dict(metadata)


def get_entity_metadata(entity_name: str) -> EntityMetadata:
  url = DARWIN_OFS_V2_ADMIN_HOST + DARWIN_OFS_V2_ADMIN_ENTITY_METADATA_ENDPOINT
  params = {"name": entity_name}

  response = requests.request("GET", url, params=params)
  if response.status_code != 200:
    parse_api_exception(response)
  metadata = MetadataResponse.from_dict(DataResponse.from_dict(response.json()).data).metadata
  return EntityMetadata.from_dict(metadata)


def create_entity(create_entity_request: CreateEntityRequest) -> Entity:
  url = DARWIN_OFS_V2_ADMIN_HOST + DARWIN_OFS_V2_ADMIN_ENTITY_ENDPOINT

  response = requests.request("POST", url, json=create_entity_request.to_dict())
  if response.status_code != 200:
    parse_api_exception(response)

  return CreateEntityResponse.from_dict(DataResponse.from_dict(response.json()).data).entity


def create_feature_group(create_feature_group_request: CreateFeatureGroupRequest, upgrade: bool = False) -> (FeatureGroup, str):
  url = DARWIN_OFS_V2_ADMIN_HOST + DARWIN_OFS_V2_ADMIN_FG_ENDPOINT

  headers = {}
  if upgrade:
    headers = {"upgrade": "true"}
  response = requests.request("POST", url, json=create_feature_group_request.to_dict(), headers=headers)
  if response.status_code != 200:
    parse_api_exception(response)
  create_response = CreateFeatureGroupResponse.from_dict(DataResponse.from_dict(response.json()).data)
  return (create_response.feature_group, create_response.version)


def get_feature_group_latest_version(feature_group_name: str) -> VersionMetadata:
  url = DARWIN_OFS_V2_ADMIN_HOST + DARWIN_OFS_V2_ADMIN_FG_LATEST_VERSION_ENDPOINT
  params = {"name": feature_group_name}

  response = requests.request("GET", url, params=params)
  if response.status_code != 200:
    parse_api_exception(response)

  return VersionMetadata.from_dict(DataResponse.from_dict(response.json()).data)


def update_feature_group_state(state: State, feature_group_name: str, feature_group_version: None) -> None:
  url = DARWIN_OFS_V2_ADMIN_HOST + DARWIN_OFS_V2_ADMIN_FG_ENDPOINT
  params = {"name": feature_group_name}
  if feature_group_version is not None:
    params["version"] = feature_group_version

  body = {
    "state": state
  }
  response = requests.request("PUT", url, params=params, json=body)
  if response.status_code != 200:
    parse_api_exception(response)
