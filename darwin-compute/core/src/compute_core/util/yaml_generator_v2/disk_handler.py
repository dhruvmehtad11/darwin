from abc import ABC
from typing import Optional

from compute_core.constant.constants import ADDITIONAL_DISK_MOUNT_PATH
from compute_core.dto.node_selector_dto import NodeSelector
from compute_core.dto.remote_command_dto import RemoteCommandDto
from compute_core.util.utils import set_handler_status, add_volume_mount, add_volume, add_node_selector
from compute_core.util.yaml_generator_v2.base_class import ConfigHandler
from compute_model.compute_cluster import ComputeClusterDefinition
from compute_model.worker_group import WorkerGroup


class DiskMounter(ABC):
    def __init__(self, node_selector):
        self.node_selector = node_selector

    def update_volume_mounts(self, group: dict):
        pass

    def update_volumes(self, group: dict):
        pass

    def update_values(self, group: dict):
        group = self.update_volumes(group)
        group = self.update_volume_mounts(group)
        add_node_selector(group, [NodeSelector("app", self.node_selector)])


class NVMEDiskMounter(DiskMounter):
    def __init__(self):
        super().__init__("nvme")

    def update_volume_mounts(self, group: dict):
        return add_volume_mount(group, {"name": "disk-nvme", "mountPath": ADDITIONAL_DISK_MOUNT_PATH})

    def update_volumes(self, group: dict):
        """
        Volume path is configured in the darwin-nvme nodepool
        example - https://github.com/dream11/darwin-karpenter/pull/78/files#diff-4f8a0fe95b42bb292be410d736f1e621a6608b9e719aadad8bbbd4994b7d7300
        """
        return add_volume(
            group,
            {"name": "disk-nvme", "hostPath": {"path": "/mnt/disk-nvme", "type": "DirectoryOrCreate"}},
        )


class EBSDiskMounter(DiskMounter):
    def __init__(self):
        super().__init__("ebs-autoscale")

    def update_volume_mounts(self, group: dict):
        return add_volume_mount(group, {"name": "disk-ebs", "mountPath": ADDITIONAL_DISK_MOUNT_PATH})

    def update_volumes(self, group: dict):
        """
        Volume path is configured in the darwin-ebs-autoscale nodepool
        example - https://github.com/dream11/darwin-karpenter/pull/78/files#diff-7748a2a129217fa6dc552b63108ac0c4ec2916cf63b1f783bd2e253acbd2b396
        """
        return add_volume(
            group,
            {"name": "disk-ebs", "hostPath": {"path": "/mnt/raid-ebs", "type": "DirectoryOrCreate"}},
        )


class DiskHandler(ConfigHandler):
    def __init__(self, next_handler=None):
        super().__init__(next_handler)
        self.additional_ebs = False
        self.is_request_processed = False

    def _update_additional_wg(self, worker_groups: list[WorkerGroup], group: dict):
        for i in range(len(worker_groups)):
            mounter = self._get_mounter(worker_groups[i].node.node_type)
            if mounter:
                mounter.update_values(group["wg" + str(i + 1)])
                self.is_request_processed = True

    def _get_mounter(self, node_type: str) -> Optional[DiskMounter]:
        if node_type == "disk":
            return NVMEDiskMounter()
        elif self.additional_ebs:
            return EBSDiskMounter()
        return None

    def _process_request(self, compute_request: ComputeClusterDefinition, values: dict) -> bool:
        """
        Returns True if request is processed else False
        """
        self.additional_ebs = "ebs-autoscale" in compute_request.tags

        head_mounter = self._get_mounter(compute_request.head_node.node.node_type)
        if head_mounter:
            head_mounter.update_values(values["head"])
            self.is_request_processed = True

        if not values.get("worker").get("disabled"):
            worker_mounter = self._get_mounter(compute_request.worker_group[0].node.node_type)
            if worker_mounter:
                worker_mounter.update_values(values["worker"])
                self.is_request_processed = True
        if "additionalWorkerGroups" in values.keys():
            self._update_additional_wg(compute_request.worker_group[1:], values["additionalWorkerGroups"])
        return self.is_request_processed

    def handle(
        self,
        values: dict,
        compute_request: ComputeClusterDefinition,
        env: str,
        step_status_list: list,
        remote_commands: list[RemoteCommandDto] = None,
    ):
        if self._process_request(compute_request, values):
            step_status_list = set_handler_status("disk_handler", "SUCCESS", step_status_list)
        else:
            step_status_list = set_handler_status("disk_handler", "SKIPPED", step_status_list)
        return super().handle(values, compute_request, env, step_status_list, remote_commands)
