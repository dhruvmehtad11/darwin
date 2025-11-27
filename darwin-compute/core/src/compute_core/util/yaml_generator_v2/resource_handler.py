from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.util.utils import set_handler_status
from compute_core.util.yaml_generator import update_resource
from compute_core.util.yaml_generator_v2.base_class import ConfigHandler
from compute_model.compute_cluster import ComputeClusterDefinition


class ResourceUpdateHandler(ConfigHandler):
    """
    This class is responsible for updating the resources in the yaml file for the cluster
    """

    def update_additional_worker_group(self, values, worker_node_list: list):
        for i in range(len(worker_node_list)):
            wg = values["additionalWorkerGroups"][f"wg{i+1}"]
            wg["resources"] = update_resource(
                worker_node_list[i].node.cores,
                worker_node_list[i].node.memory,
                getattr(worker_node_list[i].node, "gpu_count", 0),
            )

    def handle(
        self,
        values: dict,
        compute_request: ComputeClusterDefinition,
        env: str,
        step_status_list: list,
        remote_commands: list[RemoteCommandDto] = None,
    ):
        values["head"]["resources"] = update_resource(
            compute_request.head_node.node.cores,
            compute_request.head_node.node.memory,
            getattr(compute_request.head_node.node, "gpu_count", 0),
        )

        if compute_request.worker_group and len(compute_request.worker_group) > 0:
            values["worker"]["resources"] = update_resource(
                compute_request.worker_group[0].node.cores,
                compute_request.worker_group[0].node.memory,
                getattr(compute_request.worker_group[0].node, "gpu_count", 0),
            )

            if len(compute_request.worker_group) > 1:
                self.update_additional_worker_group(values, compute_request.worker_group[1:])

        step_status_list = set_handler_status("resource_handler", "SUCCESS", step_status_list)
        return super().handle(values, compute_request, env, step_status_list, remote_commands)
