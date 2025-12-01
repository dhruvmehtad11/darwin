from dataclasses import dataclass, field
from typing import List, Any

from dataclasses_json import dataclass_json, LetterCase


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class WriteFeaturesResponse:
  feature_group_name: str
  feature_group_version: str
  feature_columns: List[str]
  successful_rows: List[List[Any]]
  failed_rows: List[List[Any]]
