from loguru import logger

from compute_model.package import Package, PackageSource, PackageDetails


def test_create_with_yaml(compute):
    file_path = "request.yaml"
    resp = compute.create_with_yaml(file_path)
    logger.debug(f"Create with yaml response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_get_cluster_info(compute, compute_cluster_id):
    resp = compute.get_info("compute_cluster_id")
    logger.debug(f"Get cluster info response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_get_cluster_details(compute, compute_cluster_id):
    resp = compute.get_details(compute_cluster_id)
    logger.debug(f"Get cluster metadata response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_start_cluster(compute, compute_cluster_id):
    resp = compute.start(compute_cluster_id)
    logger.debug(f"Start cluster response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_stop_cluster(compute, compute_cluster_id):
    resp = compute.stop(compute_cluster_id)
    logger.debug(f"Stop cluster response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_restart_cluster(compute, compute_cluster_id):
    resp = compute.restart(compute_cluster_id)
    logger.debug(f"Restart cluster response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_update_cluster_with_yaml(compute, compute_cluster_id):
    file_path = "request.yaml"
    resp = compute.update_with_yaml(compute_cluster_id, file_path)
    logger.debug(f"Update cluster with yaml response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_update_cluster(compute, compute_cluster_id, updated_compute_cluster_def):
    resp = compute.update(compute_cluster_id, updated_compute_cluster_def)
    logger.debug(f"Update cluster response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_get_packages(compute, compute_cluster_id):
    """
    Test getting packages installed on a cluster
    """
    resp = compute.get_packages(compute_cluster_id)
    logger.debug(f"Get packages response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_install_package(compute, compute_cluster_id):
    """
    Test installing a package on a cluster
    """
    install_request = Package(source=PackageSource.PYPI, body=PackageDetails(name="test-library", version="1.0.0"))
    resp = compute.install_package(compute_cluster_id, install_request)
    logger.debug(f"Install package response: {resp}")
    assert resp["status"] == "SUCCESS"


def test_uninstall_packages(compute, compute_cluster_id):
    """
    Test uninstalling packages from a cluster
    """
    uninstall_request = [1, 2, 3]  # Replace with actual package IDs
    resp = compute.uninstall_packages(compute_cluster_id, uninstall_request)
    logger.debug(f"Uninstall packages response: {resp}")
    assert resp["status"] == "SUCCESS"
