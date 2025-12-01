from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.util.utils import set_handler_status
from compute_core.util.yaml_generator_v2.base_class import ConfigHandler
from compute_model.compute_cluster import ComputeClusterDefinition


class ServiceAccountHandler(ConfigHandler):
    """
    This class is responsible for updating the serviceAccountName in the yaml file for the cluster
    """

    @staticmethod
    def update_service_account(values: dict, compute_request: ComputeClusterDefinition):
        values["head"]["serviceAccountName"] = compute_request.advance_config.instance_role
        values["worker"]["serviceAccountName"] = compute_request.advance_config.instance_role

        if "additionalWorkerGroups" in values.keys():
            for wg in values["additionalWorkerGroups"]:
                values["additionalWorkerGroups"][wg][
                    "serviceAccountName"
                ] = compute_request.advance_config.instance_role

    def handle(
        self,
        values: dict,
        compute_request: ComputeClusterDefinition,
        env: str,
        step_status_list: list,
        remote_commands: list[RemoteCommandDto] = None,
    ):
        self.update_service_account(values, compute_request)

        step_status_list = set_handler_status("service_account_handler", "SUCCESS", step_status_list)
        return super().handle(values, compute_request, env, step_status_list, remote_commands)
