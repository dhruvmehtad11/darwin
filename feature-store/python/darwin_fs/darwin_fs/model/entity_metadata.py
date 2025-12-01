from dataclasses import dataclass, field
from dataclasses_json import dataclass_json, config, LetterCase
from typing import List

from darwin_fs.constant.constants import State
from darwin_fs.model.entity import Entity


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class EntityMetadata:
  name: str
  entity: Entity
  state: State
  owner: str
  tags: List[str]
  description: str
  created_at: int
  updated_at: int