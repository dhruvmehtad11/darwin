from dataclasses import dataclass

from dataclasses_json import DataClassJsonMixin


@dataclass
class MetaData(DataClassJsonMixin):
    """Base class for the meta data which can be serialised and stored in DAO"""
