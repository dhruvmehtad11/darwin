from dataclasses import *
from typeguard import typechecked
from typing import Optional


@dataclass
@typechecked
class Project:
    user_id: str
    name: str
    cloned_from: Optional[str]

    def __init__(self, user_id: str, name: str, cloned_from: str = None):
        self.user_id = user_id
        self.name = name
        self.cloned_from = cloned_from
