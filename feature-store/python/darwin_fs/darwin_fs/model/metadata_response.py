import json
from dataclasses import dataclass
from typing import TypeVar, Generic

from dataclasses_json import dataclass_json, LetterCase

T = TypeVar("T")


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class MetadataResponse(Generic[T]):
  metadata: T
