from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_model.compute_cluster import ComputeClusterDefinition


class ConfigHandler:
    """
    This is the base class for all the handlers of the yaml generator
    """

    def __init__(self, next_handler=None):
        self.next_handler = next_handler

    def set_next(self, handler):
        self.next_handler = handler

    def handle(
        self,
        values: dict,
        compute_request: ComputeClusterDefinition,
        env: str,
        step_status_list: list,
        remote_commands: list[RemoteCommandDto] = None,
    ):
        if self.next_handler:
            return self.next_handler.handle(values, compute_request, env, step_status_list, remote_commands)
        return values
