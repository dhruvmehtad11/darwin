from dataclasses import dataclass
from typing import Optional

from dataclasses_json import DataClassJsonMixin

from compute_core.dto.remote_command_dto import RemoteCommandStatus, RemoteCommandTarget


@dataclass
class RunningRemoteCommandDto(DataClassJsonMixin):
    execution_id: str
    cluster_id: str
    target: RemoteCommandTarget
    status: RemoteCommandStatus


@dataclass
class RemoteCommandExecutionUpdateStatusDTO(DataClassJsonMixin):
    status: RemoteCommandStatus
    error_logs_path: Optional[str] = None
    error_code: Optional[str] = None
