from dataclasses import dataclass
from typing import List, Any


@dataclass
class SuccessfulKey:
  key: List[Any]
  features: List[Any]

  @staticmethod
  def from_dict(data):
    return SuccessfulKey(**data)
