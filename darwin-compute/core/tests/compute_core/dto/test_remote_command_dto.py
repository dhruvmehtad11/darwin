from compute_core.dto.remote_command_dto import RemoteCommandExecuteDCMDto


def test_remote_command_execute_dcm_dto():
    dto = RemoteCommandExecuteDCMDto(
        kube_cluster="kube_cluster",
        kube_namespace="kube_namespace",
        label_selector="label_selector",
        container_name="container_name",
        command="command",
    )
    assert dto.kube_cluster == "kube_cluster"
    assert dto.kube_namespace == "kube_namespace"
    assert dto.label_selector == "label_selector"
    assert dto.container_name == "container_name"
    assert dto.command == "command"


def test_remote_command_execute_dcm_dto_dict():
    dto = RemoteCommandExecuteDCMDto(
        kube_cluster="kube_cluster",
        kube_namespace="kube_namespace",
        label_selector="label_selector",
        container_name="container_name",
        command="command",
    )
    assert dto.to_dict(encode_json=True) == {
        "kube_cluster": "kube_cluster",
        "kube_namespace": "kube_namespace",
        "label_selector": "label_selector",
        "container_name": "container_name",
        "command": "command",
    }
