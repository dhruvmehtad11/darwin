from dataclasses import dataclass

from dataclasses_json import DataClassJsonMixin


@dataclass
class WorkspaceAndCodespaceResponse(DataClassJsonMixin):
    project_id: int
    project_name: str
    codespace_id: int
    codespace_name: str
