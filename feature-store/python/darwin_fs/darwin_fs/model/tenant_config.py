from dataclasses import dataclass

from dataclasses_json import dataclass_json, LetterCase


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class TenantConfig:
  read: str
  write: str
  consume: str
