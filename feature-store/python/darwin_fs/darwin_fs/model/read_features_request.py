from dataclasses import dataclass
from typing import List, Optional

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.model.primary_keys import PrimaryKeys


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class ReadFeaturesRequest:
  feature_group_name: str
  feature_columns: List[str]
  primary_keys: PrimaryKeys
  feature_group_version: Optional[str] = None
