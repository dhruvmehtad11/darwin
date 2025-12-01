import json
from dataclasses import dataclass
from typing import TypeVar, Generic

from dataclasses_json import LetterCase, dataclass_json

T = TypeVar("T")


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class DataResponse(Generic[T]):
  data: T
