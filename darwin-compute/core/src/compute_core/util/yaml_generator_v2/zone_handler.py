from random import randint

from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.util.utils import set_handler_status
from compute_core.util.yaml_generator import ZONES
from compute_core.util.yaml_generator_v2.base_class import ConfigHandler
from compute_model.compute_cluster import ComputeClusterDefinition


class ZoneHandler(ConfigHandler):
    """
    This class is responsible for adding zone information to the yaml file for the cluster
    """

    def _to_process(self, compute_request):
        return "zonal" in compute_request.tags

    def handle(
        self,
        values: dict,
        compute_request: ComputeClusterDefinition,
        env: str,
        step_status_list: list,
        remote_commands: list[RemoteCommandDto] = None,
    ):
        if self._to_process(compute_request):
            zone = ZONES[randint(0, 2)]
            values["head"]["nodeSelector"]["topology.kubernetes.io/zone"] = zone
            values["worker"]["nodeSelector"]["topology.kubernetes.io/zone"] = zone

            if "additionalWorkerGroups" in values.keys():
                for i in values["additionalWorkerGroups"]:
                    values["additionalWorkerGroups"][i]["nodeSelector"]["topology.kubernetes.io/zone"] = zone
            step_status_list = set_handler_status("zone_handler", "SUCCESS", step_status_list)
        else:
            step_status_list = set_handler_status("zone_handler", "SKIPPED", step_status_list)
        return super().handle(values, compute_request, env, step_status_list, remote_commands)
