from compute_core.constant.config import Config
from compute_core.dto.remote_command_dto import RemoteCommandDto, RemoteCommandTarget
from compute_core.util.utils import set_handler_status
from compute_core.util.yaml_generator_v2.base_class import ConfigHandler
from compute_model.compute_cluster import ComputeClusterDefinition


class RemoteCommandHandler(ConfigHandler):
    """
    This class is responsible for updating the remote commands in the yaml file for the cluster
    """

    def update_remote_command(
        self, config: Config, remote_command_values: dict, remote_commands: list[RemoteCommandDto]
    ):
        remote_command_values["statusReportApi"] = config.remote_command_config["status_report_api"]
        remote_command_values["statusReportInterval"] = config.remote_command_config["status_report_interval"]
        remote_command_values["logsS3Bucket"] = config.remote_command_config["logs_s3_bucket"]
        remote_command_values["logsS3Key"] = config.remote_command_config["logs_s3_key"]
        remote_command_values["commands"] = {"head": [], "worker": []}
        if remote_commands:
            for remote_command in remote_commands:
                data = {
                    "executionId": remote_command.execution_id,
                    "command": remote_command.command,
                    "timeout": remote_command.timeout,
                }
                if remote_command.target in [RemoteCommandTarget.head, RemoteCommandTarget.cluster]:
                    remote_command_values["commands"]["head"].append(data)
                if remote_command.target in [RemoteCommandTarget.worker, RemoteCommandTarget.cluster]:
                    remote_command_values["commands"]["worker"].append(data)

    def handle(
        self,
        values: dict,
        compute_request: ComputeClusterDefinition,
        env: str,
        step_status_list: list,
        remote_commands: list[RemoteCommandDto] = None,
    ):
        config = Config()
        self.update_remote_command(config, values["remoteCommand"], remote_commands)
        step_status_list = set_handler_status("remote_command_handler", "SUCCESS", step_status_list)

        return super().handle(values, compute_request, env, step_status_list, remote_commands)
