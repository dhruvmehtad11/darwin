from compute_model.compute_cluster import ComputeClusterDefinition
from darwin_compute.util.utils import read_yaml


def test_yaml_to_compute_model():
    file_path = "request.yaml"
    req_dict = read_yaml(file_path)
    compute_request: ComputeClusterDefinition = ComputeClusterDefinition.from_dict(req_dict)
    print(compute_request)
    assert True


def test_yaml_to_app_layer_model_conversion():
    file_path = "request.yaml"
    req_dict = read_yaml(file_path)
    compute_request: ComputeClusterDefinition = ComputeClusterDefinition.from_dict(req_dict)
    app_layer_model = compute_request.convert()
    print("\n", app_layer_model)
    assert True
