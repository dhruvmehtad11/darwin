from datetime import datetime
from enum import Enum
from pydantic import BaseModel, validator, Field
from typing import Optional, List


class RuntimeClassEnum(str, Enum):
    CPU = "CPU"
    GPU = "GPU"
    CUSTOM = "CUSTOM"


class RuntimeTypeEnum(str, Enum):
    RAY_ONLY = "Ray Only"
    RAY_AND_SPARK = "Ray and Spark"
    OTHERS = "Others"
    CREATED_BY_ME = "Created By Me"
    CREATED_BY_OTHERS = "Created By Others"


class ComponentNameEnum(str, Enum):
    RAY = "Ray"
    SPARK = "Spark"
    PYTHON = "Python"
    R = "R"
    SCYLLA = "Scylla"


class RuntimeComponent(BaseModel):
    name: ComponentNameEnum
    version: str


class RuntimeV2Request(BaseModel):
    runtime: str
    class_: RuntimeClassEnum = Field(alias="class")
    type: Optional[RuntimeTypeEnum] = None
    image: str
    reference_link: Optional[str] = None
    components: Optional[List[RuntimeComponent]] = None
    user: str  # modifies created_by and last_updated_by in case of create and update respectively
    set_as_default: bool = False
    spark_connect: bool = False
    spark_auto_init: bool = False

    @validator("type")
    def validate_type_for_class(cls, v, values):
        if "class_" in values:
            runtime_class = values["class_"]
            if runtime_class == RuntimeClassEnum.CUSTOM and v is not None:
                raise ValueError("Type must be NULL for CUSTOM class")
            if runtime_class in [RuntimeClassEnum.CPU, RuntimeClassEnum.GPU]:
                if v is None:
                    raise ValueError("Type is required for CPU and GPU class")
                elif v not in [RuntimeTypeEnum.RAY_ONLY, RuntimeTypeEnum.RAY_AND_SPARK, RuntimeTypeEnum.OTHERS]:
                    raise ValueError(
                        "Incorrect Type for CPU and GPU class. Must be 'Ray Only', 'Ray and Spark' or 'Others'"
                    )
            # type is closely related to class. So, specifying type without class is invalid
            if runtime_class == None and v is not None:
                raise ValueError("Invalid Input. Specify the class first and then the type")
        return v

    @validator("set_as_default")
    def validate_set_as_default(cls, v, values):
        if v and values["class_"] == RuntimeClassEnum.CUSTOM:
            raise ValueError("Cannot set default for CUSTOM class")
        return v


class RuntimeV2Details(BaseModel):
    id: int
    runtime: str
    class_: RuntimeClassEnum = Field(alias="class")
    type: Optional[RuntimeTypeEnum] = None
    image: str
    reference_link: str = None
    components: Optional[List[RuntimeComponent]] = None
    created_by: str  # user who originally created the runtime
    created_at: datetime  # gets auto-updated on create
    last_updated_by: str  # user who last updated the runtime
    last_updated_at: datetime  # gets auto-updated on update
    is_deleted: bool = False
    spark_connect: bool = False
    spark_auto_init: bool = False


class GetRuntimesRequest(BaseModel):
    search_query: Optional[str] = None
    offset: int = 0
    page_size: int = 100
    class_: Optional[RuntimeClassEnum] = Field(alias="class")
    type: Optional[RuntimeTypeEnum] = None
    is_deleted: Optional[bool] = False

    @validator("type")
    def validate_type_for_class(cls, v, values):
        if "class_" in values:
            runtime_class = values["class_"]
            if v is not None:
                if (
                    runtime_class == None
                ):  # type is closely related to class. So, specifying type without class is invalid
                    raise ValueError("Invalid Input. Specify the class first and then the type")
                elif runtime_class == RuntimeClassEnum.CUSTOM:
                    if v != RuntimeTypeEnum.CREATED_BY_ME and v != RuntimeTypeEnum.CREATED_BY_OTHERS:
                        raise ValueError(
                            "Incorrect Type for CUSTOM class. Must be 'Created By Me' or 'Created By Others'"
                        )
                else:
                    if v not in [RuntimeTypeEnum.RAY_ONLY, RuntimeTypeEnum.RAY_AND_SPARK, RuntimeTypeEnum.OTHERS]:
                        raise ValueError(
                            "Incorrect Type for CPU and GPU class. Must be 'Ray Only', 'Ray and Spark' or 'Others'"
                        )
        return v
