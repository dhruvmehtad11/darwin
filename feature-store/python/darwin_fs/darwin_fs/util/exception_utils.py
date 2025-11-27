from darwin_fs.model.error_response import ErrorResponse
from darwin_fs.exception import SdkException, ApiException


def parse_api_exception(response):
  try:
    error_response = ErrorResponse.from_dict(response.json())
  except Exception as e:
    raise SdkException(f"unknown exception while parsing api response form ofs: {e}\n\tresponse: {response.text}")
  raise ApiException(error_response.error)