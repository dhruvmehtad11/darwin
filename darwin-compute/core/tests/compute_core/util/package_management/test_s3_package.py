from unittest import TestCase

from compute_app_layer.models.request.library import InstallRequest, LibraryRequest
from compute_core.constant.constants import (
    PYTHON_PACKAGE_DOWNLOAD_DIR,
    JAVA_PACKAGE_DOWNLOAD_DIR,
    S3_PACKAGE_DOWNLOAD_DIR,
)
from compute_core.dto.library_dto import LibrarySource
from compute_core.util.package_management.impl.s3_package import S3Package


class TestS3Package(TestCase):
    def test_get_installation_command_for_whl_from_s3(self):
        file_name = "test.whl"
        s3_path = "s3://d11-mlstag/darwin_artifactory_stag/test.whl"
        expected = [
            f"aws s3 cp {s3_path} {PYTHON_PACKAGE_DOWNLOAD_DIR}/{file_name}",
            f"pip install {PYTHON_PACKAGE_DOWNLOAD_DIR}/{file_name}",
        ]
        library = InstallRequest(source=LibrarySource.S3.value, body=LibraryRequest(name=file_name, path=s3_path))
        self.assertEqual(S3Package(library).get_installation_command(), expected)

    def test_get_installation_command_for_txt_from_s3(self):
        file_name = "test.txt"
        s3_path = "s3://d11-mlstag/darwin_artifactory_stag/test.txt"
        expected = [
            f"aws s3 cp {s3_path} {PYTHON_PACKAGE_DOWNLOAD_DIR}/{file_name}",
            f"pip install -r {PYTHON_PACKAGE_DOWNLOAD_DIR}/{file_name}",
        ]
        library = InstallRequest(source=LibrarySource.S3.value, body=LibraryRequest(name=file_name, path=s3_path))
        self.assertEqual(S3Package(library).get_installation_command(), expected)

    def test_get_installation_command_for_jar_from_s3(self):
        file_name = "test.jar"
        s3_path = "s3://d11-mlstag/darwin_artifactory_stag/test.jar"
        expected = [
            f"aws s3 cp {s3_path} {JAVA_PACKAGE_DOWNLOAD_DIR}/{file_name}",
        ]
        library = InstallRequest(source=LibrarySource.S3.value, body=LibraryRequest(name=file_name, path=s3_path))
        self.assertEqual(S3Package(library).get_installation_command(), expected)

    def test_get_installation_command_for_tar_from_s3(self):
        file_name = "test.tar"
        folder_name = "test"
        s3_path = "s3://d11-mlstag/darwin_artifactory_stag/test.tar"
        expected = [
            f"aws s3 cp {s3_path} {S3_PACKAGE_DOWNLOAD_DIR}/{file_name}",
            f"tar -xvf {S3_PACKAGE_DOWNLOAD_DIR}/{file_name}",
            f"for file in {S3_PACKAGE_DOWNLOAD_DIR}/{folder_name}/*; do if [[ $file == *.whl ]]; then "
            f"pip install $file; elif [[ $file == *.txt ]]; then "
            f"pip install -r $file; elif [[ $file == *.jar ]]; then "
            f"mkdir -p {JAVA_PACKAGE_DOWNLOAD_DIR} && mv $file {JAVA_PACKAGE_DOWNLOAD_DIR}/$(basename $file); "
            f"fi; done;",
        ]
        library = InstallRequest(source=LibrarySource.S3.value, body=LibraryRequest(name=file_name, path=s3_path))
        self.assertEqual(S3Package(library).get_installation_command(), expected)

    def test_get_installation_command_for_zip_from_s3(self):
        file_name = "test.zip"
        folder_name = "test"
        s3_path = "s3://d11-mlstag/darwin_artifactory_stag/test.zip"
        expected = [
            f"aws s3 cp {s3_path} {S3_PACKAGE_DOWNLOAD_DIR}/{file_name}",
            f"unzip {S3_PACKAGE_DOWNLOAD_DIR}/{file_name} -d {S3_PACKAGE_DOWNLOAD_DIR}",
            f"for file in {S3_PACKAGE_DOWNLOAD_DIR}/{folder_name}/*; do if [[ $file == *.whl ]]; then "
            f"pip install $file; elif [[ $file == *.txt ]]; then "
            f"pip install -r $file; elif [[ $file == *.jar ]]; then "
            f"mkdir -p {JAVA_PACKAGE_DOWNLOAD_DIR} && mv $file {JAVA_PACKAGE_DOWNLOAD_DIR}/$(basename $file); "
            f"fi; done;",
        ]
        library = InstallRequest(source=LibrarySource.S3.value, body=LibraryRequest(name=file_name, path=s3_path))
        self.assertEqual(S3Package(library).get_installation_command(), expected)

    def test_get_installation_command_for_tar_gz_from_s3(self):
        file_name = "test.tar.gz"
        folder_name = "test.gz"
        s3_path = "s3://d11-mlstag/darwin_artifactory_stag/test.tar.gz"
        expected = [
            f"aws s3 cp {s3_path} {S3_PACKAGE_DOWNLOAD_DIR}/{file_name}",
            f"tar -xvf {S3_PACKAGE_DOWNLOAD_DIR}/{file_name}",
            f"for file in {S3_PACKAGE_DOWNLOAD_DIR}/{folder_name}/*; do if [[ $file == *.whl ]]; then "
            f"pip install $file; elif [[ $file == *.txt ]]; then "
            f"pip install -r $file; elif [[ $file == *.jar ]]; then "
            f"mkdir -p {JAVA_PACKAGE_DOWNLOAD_DIR} && mv $file {JAVA_PACKAGE_DOWNLOAD_DIR}/$(basename $file); "
            f"fi; done;",
        ]
        library = InstallRequest(source=LibrarySource.S3.value, body=LibraryRequest(name=file_name, path=s3_path))
        self.assertEqual(S3Package(library).get_installation_command(), expected)
