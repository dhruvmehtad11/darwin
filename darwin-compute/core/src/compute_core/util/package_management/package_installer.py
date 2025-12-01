from compute_app_layer.models.remote_command.remote_command_request import RemoteCommandRequest
from compute_app_layer.models.request.library import InstallRequest
from compute_core.dto.library_dto import LibraryDTO, LibraryStatus
from compute_core.dto.remote_command_dto import RemoteCommandStatus
from compute_core.remote_command import RemoteCommand
from compute_core.util.package_management.package_factory import PackageFactory


class PackageInstaller:
    def __init__(self, remote_command_manager: RemoteCommand = None):
        self._remote_command_manager = remote_command_manager

    def install(self, libraries: list[InstallRequest], cluster_id: str, cluster_status: str) -> list[LibraryDTO]:
        remote_commands = []
        dtos = []
        for library in libraries:
            package = PackageFactory.get_package(library)
            package.dto.cluster_id = cluster_id
            dtos.append(package.dto)
            remote_commands.append(RemoteCommandRequest(command=package.get_installation_command()).convert())

        if cluster_status == "inactive":
            resp = self._remote_command_manager.add_to_cluster(cluster_id, remote_commands)
            for i in range(len(dtos)):
                dtos[i].execution_id = resp["remote_commands"][i]["execution_id"]
                dtos[i].status = LibraryStatus.CREATED
        else:
            resp = self._remote_command_manager.execute_multiple_on_cluster(cluster_id, remote_commands)
            for i in range(len(dtos)):
                dtos[i].execution_id = resp["remote_commands"][i]["execution_id"]
                dtos[i].status = LibraryStatus.RUNNING
                remote_commands[i].status = RemoteCommandStatus.RUNNING

        return dtos
