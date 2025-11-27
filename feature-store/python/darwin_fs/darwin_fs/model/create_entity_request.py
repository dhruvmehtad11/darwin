from dataclasses import dataclass
from typing import List

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.model.entity import Entity


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class CreateEntityRequest:
  entity: Entity
  owner: str
  tags: List[str]
  description: str
