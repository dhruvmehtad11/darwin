from unittest import TestCase

from compute_app_layer.models.request.library import InstallRequest, LibraryRequest
from compute_core.dto.library_dto import LibrarySource
from compute_core.util.package_management.impl.python_package import PythonPackage


class TestPythonPackage(TestCase):
    def test_get_installation_command_with_package_name(self):
        package_name = "test"
        expected = [f"pip install {package_name}"]
        library = InstallRequest(source=LibrarySource.PYPI.value, body=LibraryRequest(name=package_name))
        self.assertEqual(PythonPackage(library).get_installation_command(), expected)

    def test_get_installation_command_with_package_name_and_version(self):
        package_name = "test"
        package_version = "1.0.0"
        expected = [f"pip install {package_name}=={package_version}"]
        library = InstallRequest(
            source=LibrarySource.PYPI.value, body=LibraryRequest(name=package_name, version=package_version)
        )
        self.assertEqual(PythonPackage(library).get_installation_command(), expected)

    def test_get_installation_command_with_package_name_and_index_url(self):
        package_name = "test"
        index_url = "https://test.com"
        expected = [f"pip install {package_name} --index-url {index_url} --trusted-host test.com"]
        library = InstallRequest(
            source=LibrarySource.PYPI.value, body=LibraryRequest(name=package_name, path=index_url)
        )
        self.assertEqual(PythonPackage(library).get_installation_command(), expected)

    def test_get_installation_command_with_package_name_version_and_index_url(self):
        package_name = "test"
        package_version = "1.0.0"
        index_url = "https://test.com"
        expected = [f"pip install {package_name}=={package_version} --index-url {index_url} --trusted-host test.com"]
        library = InstallRequest(
            source=LibrarySource.PYPI.value,
            body=LibraryRequest(name=package_name, version=package_version, path=index_url),
        )
        self.assertEqual(PythonPackage(library).get_installation_command(), expected)
