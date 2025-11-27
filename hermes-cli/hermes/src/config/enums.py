from enum import Enum


class ProjectType(str, Enum):
    FASTAPI = "api"
    WORKFLOW = "workflow[NOT SUPPORTED YET]"
    RAYSERVE = "rayserve[NOT SUPPORTED YET]"


class InferenceType(str, Enum):
    OFFLINE = "offline"
    ONLINE = "online"
    BOTH = "both"


class FieldType(str, Enum):
    STRING = "string"
    INTEGER = "int"
    FLOAT = "float"
    BOOLEAN = "boolean"
    LIST = "list"
    DICT = "dict"
    ARRAY = "array"
