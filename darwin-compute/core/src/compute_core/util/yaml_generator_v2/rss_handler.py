from random import randint

from compute_core.constant.constants import RSS_MOUNT_PATH
from compute_core.dao.cluster_dao import ClusterDao
from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.util.utils import set_handler_status
from compute_core.util.yaml_generator import env_variable, add_volume_mount
from compute_core.util.yaml_generator_v2.base_class import ConfigHandler
from compute_model.compute_cluster import ComputeClusterDefinition


def to_process_rss(compute_request):
    return "rss" in compute_request.tags


class RssHandler(ConfigHandler):
    """
    This class is responsible for updating the rss in the yaml file for the cluster if rss tag is present in the request
    """

    def __init__(self, next_handler=None):
        super().__init__(next_handler)

    def _remove_rss_env_variable(self, values):
        values["common"]["containerEnv"] = [env for env in values["common"]["containerEnv"] if env["name"] != "RSS"]

    def update_env_variable(self, values):
        self._remove_rss_env_variable(values)
        values["common"]["containerEnv"].append(env_variable("RSS", True))

    def update_rss_claims(self, values, env):
        cluster_dao = ClusterDao(env)
        rss_total_fsx_num = int(cluster_dao.get_cluster_config("RSS_TOTAL_FSX_NUM")[0]["value"])
        rss_per_fsx_claim_num = int(cluster_dao.get_cluster_config("RSS_PER_FSX_CLAIM_NUM")[0]["value"])

        rss_fsx_name = f"rss-claim-{randint(0, rss_total_fsx_num - 1)}"
        rss_claim_name = f"{rss_fsx_name}-{randint(0, rss_per_fsx_claim_num - 1)}"

        add_volume_mount("remote-shuffle", rss_claim_name, RSS_MOUNT_PATH, values["head"])
        if not values["worker"].get("disabled"):
            add_volume_mount("remote-shuffle", rss_claim_name, RSS_MOUNT_PATH, values["worker"])

        if "additionalWorkerGroups" in values.keys():
            for i in values["additionalWorkerGroups"]:
                add_volume_mount("remote-shuffle", rss_claim_name, RSS_MOUNT_PATH, values["additionalWorkerGroups"][i])

    def handle(
        self,
        values: dict,
        compute_request: ComputeClusterDefinition,
        env: str,
        step_status_list: list,
        remote_commands: list[RemoteCommandDto] = None,
    ):
        if to_process_rss(compute_request):
            self.update_env_variable(values)
            self.update_rss_claims(values, env)
            step_status_list = set_handler_status("rss_handler", "SUCCESS", step_status_list)
        else:
            step_status_list = set_handler_status("rss_handler", "SKIPPED", step_status_list)
        return super().handle(values, compute_request, env, step_status_list, remote_commands)
