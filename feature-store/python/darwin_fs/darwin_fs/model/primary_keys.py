from dataclasses import dataclass
from typing import List, Any

from dataclasses_json import dataclass_json, LetterCase


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class PrimaryKeys:
  names: List[str]
  values: List[List[Any]]