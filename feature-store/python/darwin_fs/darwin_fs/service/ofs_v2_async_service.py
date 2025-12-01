import json

from aiohttp import ClientSession

from darwin_fs.config import *
from darwin_fs.model.data_response import DataResponse
from darwin_fs.model.read_features_request import ReadFeaturesRequest
from darwin_fs.model.read_features_response import ReadFeaturesResponse
from darwin_fs.model.write_features_request import WriteFeaturesRequest
from darwin_fs.model.write_features_response import WriteFeaturesResponse
from darwin_fs.util.exception_utils import parse_api_exception


async def feature_group_write_features(client: ClientSession, write_features_request: WriteFeaturesRequest) -> WriteFeaturesResponse:
  url = DARWIN_OFS_V2_WRITER_HOST + DARWIN_OFS_V2_FG_WRITE_V2_ENDPOINT

  response = await client.request("POST", url, json=write_features_request.to_dict())
  if response.status != 200:
    parse_api_exception(response)

  response_body = await response.read()
  return WriteFeaturesResponse.from_dict(DataResponse.from_dict(json.loads(response_body)).data)


async def feature_group_read_features(client: ClientSession, read_features_request: ReadFeaturesRequest) -> ReadFeaturesResponse:
  url = DARWIN_OFS_V2_HOST + DARWIN_OFS_V2_FG_READ_ENDPOINT

  response = await client.request("GET", url, json=read_features_request.to_dict())
  if response.status != 200:
    parse_api_exception(response)

  response_body = await response.read()
  return ReadFeaturesResponse.from_dict(DataResponse.from_dict(json.loads(response_body)).data)
