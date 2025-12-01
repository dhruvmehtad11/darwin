from enum import Enum

class DataType(str, Enum):
  TEXT = "TEXT"
  ASCII = "ASCII"
  VARCHAR = "VARCHAR"
  BLOB = "BLOB"
  BOOLEAN = "BOOLEAN"
  DECIMAL = "DECIMAL"
  DOUBLE = "DOUBLE"
  FLOAT = "FLOAT"
  INT = "INT"
  BIGINT = "BIGINT"
  TIMESTAMP = "TIMESTAMP"
  TIMEUUID = "TIMEUUID"
  UUID = "UUID"
  INET = "INET"
  VARINT = "VARINT"

class FeatureGroupType(str, Enum):
  ONLINE = "ONLINE"
  OFFLINE = "OFFLINE"

class State(str, Enum):
  LIVE = "LIVE"
  ARCHIVED = "ARCHIVED"
