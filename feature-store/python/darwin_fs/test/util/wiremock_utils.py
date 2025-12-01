import json


def parse_params_and_headers(data: dict):
  res = {}
  for k in data:
    res[k] = {"equalTo": data[k]}
  return res

def parse_request_body(data: dict):
  return [{"equalToJson": json.dumps(data)}]