from typing import List, Optional

from pydantic import BaseModel, Field

from compute_app_layer.models.runtime_v2 import RuntimeTypeEnum, RuntimeClassEnum, RuntimeV2Details


class RuntimesByType(BaseModel):
    type: RuntimeTypeEnum
    count: int
    runtime_list: List[RuntimeV2Details]


class RuntimesByClass(BaseModel):
    class_: RuntimeClassEnum = Field(alias="class")
    default_runtime: Optional[RuntimeV2Details]
    total_count: int
    runtimes: List[RuntimesByType]
