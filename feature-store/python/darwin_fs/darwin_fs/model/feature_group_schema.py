import json
from dataclasses import dataclass, field
from typing import List

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.model.feature_column import FeatureColumn


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class FeatureGroupSchema:
  schema: List[FeatureColumn]
  primary_keys: List[str]
  version: str