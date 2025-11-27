from dataclasses import dataclass, field
from typing import List, Any

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.model.successful_key import SuccessfulKey


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class ReadFeaturesResponse:
  feature_group_name: str
  feature_group_version: str
  successful_keys: List[SuccessfulKey]
  failed_keys: List[List[Any]]
