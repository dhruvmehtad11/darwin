from dataclasses import dataclass, field
from typing import List

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.model.feature_group import FeatureGroup


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class CreateFeatureGroupRequest:
  feature_group: FeatureGroup
  owner: str
  tags: List[str]
  description: str
