from uuid import uuid4
from pydantic import BaseModel, Field

from compute_core.dto.remote_command_dto import RemoteCommandDto, RemoteCommandTarget, RemoteCommandStatus


class RemoteCommandRequest(BaseModel):
    execution_id: str = Field(init_var=False, default_factory=lambda: uuid4().hex)
    command: list[str]
    target: RemoteCommandTarget = Field(default=RemoteCommandTarget.cluster)
    status: RemoteCommandStatus = Field(init_var=False, default=RemoteCommandStatus.CREATED)
    timeout: int = Field(default=300)

    def convert(self) -> RemoteCommandDto:
        return RemoteCommandDto(
            execution_id=self.execution_id,
            command=" ; ".join(self.command),
            target=self.target,
            status=self.status,
            timeout=self.timeout,
        )


class AddRemoteCommandsRequest(BaseModel):
    remote_commands: list[RemoteCommandRequest]


class PodCommandExecutionStatusReportRequest(BaseModel):
    cluster_id: str
    execution_id: str
    pod_name: str
    status: RemoteCommandStatus
