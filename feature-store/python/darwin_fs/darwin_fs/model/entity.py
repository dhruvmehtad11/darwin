from dataclasses import dataclass, field
from typing import List, Optional

from dataclasses_json import config, dataclass_json, LetterCase

from darwin_fs.model.feature_column import FeatureColumn


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class Entity:
  table_name: str
  features: List[FeatureColumn]
  primary_keys: List[str]
  ttl: Optional[int] = 0

