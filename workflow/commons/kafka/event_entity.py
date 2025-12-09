from dataclasses import dataclass
from typing import TypeVar

from dataclasses_json import DataClassJsonMixin
from typeguard import typechecked

from commons.constant.constants import Entity, State

StateSubclass = TypeVar('StateSubclass', bound=State, covariant=True)


@typechecked
@dataclass
class Event(DataClassJsonMixin):
    """
    Event object is definition of any event occurring during the execution of any process.
    """
    entity: Entity
    entity_id: str
    state: StateSubclass
    metadata: dict
    timestamp: str

    def __init__(self, entity: Entity, entity_id: str, state: StateSubclass, metadata: dict, timestamp: str):
        self.entity = entity
        self.entity_id = entity_id
        self.state = state
        self.metadata = metadata
        self.timestamp = timestamp
