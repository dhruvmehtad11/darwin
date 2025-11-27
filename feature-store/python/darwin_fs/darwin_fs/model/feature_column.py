import json
from dataclasses import dataclass
from enum import Enum
from typing import List

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.constant.constants import DataType


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class FeatureColumn:
  name: str
  type: DataType
  description: str
  tags: List[str]