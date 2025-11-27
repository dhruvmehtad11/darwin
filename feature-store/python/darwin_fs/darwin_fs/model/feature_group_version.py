from dataclasses import dataclass, field

from dataclasses_json import dataclass_json, LetterCase


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class FeatureGroupVersion:
  name: str
  latest_version: str
  updated_at: int
