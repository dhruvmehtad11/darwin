from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.util.utils import set_handler_status, is_gcp_cluster, update_affinity
from compute_core.util.yaml_generator_v2.base_class import ConfigHandler
from compute_model.compute_cluster import ComputeClusterDefinition


class LongRunningClusterHandler(ConfigHandler):
    def _to_process(self, compute_request):
        return compute_request.terminate_after_minutes == -1

    def update_wg_value(self, wg: dict, compute_request: ComputeClusterDefinition):
        if is_gcp_cluster(compute_request.cloud_env):
            wg["labels"]["darwin_resource"] = compute_request.cluster_id
            update_affinity(wg, compute_request.cluster_id)
        else:
            wg["nodeSelector"]["darwin.dream11.com/resource"] = compute_request.cluster_id

    def update_head_node(self, head: dict, compute_request: ComputeClusterDefinition):
        if is_gcp_cluster(compute_request.cloud_env):
            darwin_resource = f"{compute_request.cluster_id}-head"
            head["labels"]["darwin_resource"] = darwin_resource
            update_affinity(head, darwin_resource)
        else:
            head["nodeSelector"]["darwin.dream11.com/resource"] = f"{compute_request.cluster_id}-head"

    def handle(
        self,
        values: dict,
        compute_request: ComputeClusterDefinition,
        env: str,
        step_status_list: list,
        remote_commands: list[RemoteCommandDto] = None,
    ):
        if self._to_process(compute_request):
            self.update_head_node(values["head"], compute_request)

            if compute_request.worker_group and len(compute_request.worker_group) > 0:
                self.update_wg_value(values["worker"], compute_request)
                if "additionalWorkerGroups" in values.keys():
                    for i in values["additionalWorkerGroups"]:
                        self.update_wg_value(values["additionalWorkerGroups"][i], compute_request)

            step_status_list = set_handler_status("long_running_cluster_handler", "SUCCESS", step_status_list)
        else:
            step_status_list = set_handler_status("long_running_cluster_handler", "SKIPPED", step_status_list)
        return super().handle(values, compute_request, env, step_status_list, remote_commands)
