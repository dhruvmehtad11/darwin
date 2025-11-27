import re


def remove_proto_from_url(url):
  return re.sub(r'^https?://', '', url)