from unittest import TestCase

from compute_core.constant.constants import (
    JAVA_PACKAGE_DOWNLOAD_DIR,
    S3_PACKAGE_DOWNLOAD_DIR,
    PYTHON_PACKAGE_DOWNLOAD_DIR,
)
from compute_core.util.package_management.utils import (
    get_command_to_copy_s3_file_to_local,
    get_file_name_from_url,
    get_command_to_extract_and_install_packages_in_dir,
    get_installation_command_for_tar_from_s3,
    get_installation_command_for_zip_from_s3,
    get_installation_command_for_jar_from_s3,
    get_installation_command_for_txt_from_s3,
    get_installation_command_for_wheel_from_s3,
    get_command_func_for_file_path,
    get_trusted_host_from_index_url,
)


class TestUtil(TestCase):
    def test_get_command_to_copy_s3_file_to_local(self):
        s3_file_path = "s3://test/test.txt"
        local_file_path = "/test/test.txt"
        command = get_command_to_copy_s3_file_to_local(s3_file_path, local_file_path)
        self.assertEqual(command, f"aws s3 cp {s3_file_path} {local_file_path}")

    def test_get_file_name_from_url(self):
        s3_file_path = "s3://test/test.txt"
        file_name = get_file_name_from_url(s3_file_path)
        self.assertEqual(file_name, "test.txt")

    def test_get_command_to_extract_and_install_packages_in_dir(self):
        dir_path = "/test"
        command = get_command_to_extract_and_install_packages_in_dir(dir_path)
        self.assertEqual(
            command,
            (
                f"for file in {dir_path}/*; do if [[ $file == *.whl ]]; then "
                f"pip install $file; elif [[ $file == *.txt ]]; then "
                f"pip install -r $file; elif [[ $file == *.jar ]]; then "
                f"mkdir -p {JAVA_PACKAGE_DOWNLOAD_DIR} && mv $file {JAVA_PACKAGE_DOWNLOAD_DIR}/$(basename $file); "
                f"fi; done;"
            ),
        )

    def test_get_installation_command_for_tar_from_s3(self):
        s3_file_path = "s3://test/test.tar"
        commands = get_installation_command_for_tar_from_s3(s3_file_path)
        self.assertEqual(
            commands,
            [
                f"aws s3 cp {s3_file_path} {S3_PACKAGE_DOWNLOAD_DIR}/test.tar",
                f"tar -xvf {S3_PACKAGE_DOWNLOAD_DIR}/test.tar",
                get_command_to_extract_and_install_packages_in_dir(f"{S3_PACKAGE_DOWNLOAD_DIR}/test"),
            ],
        )

    def test_get_installation_command_for_zip_from_s3(self):
        s3_file_path = "s3://test/test.zip"
        commands = get_installation_command_for_zip_from_s3(s3_file_path)
        self.assertEqual(
            commands,
            [
                f"aws s3 cp {s3_file_path} {S3_PACKAGE_DOWNLOAD_DIR}/test.zip",
                f"unzip {S3_PACKAGE_DOWNLOAD_DIR}/test.zip -d {S3_PACKAGE_DOWNLOAD_DIR}",
                get_command_to_extract_and_install_packages_in_dir(f"{S3_PACKAGE_DOWNLOAD_DIR}/test"),
            ],
        )

    def test_get_installation_command_for_jar_from_s3(self):
        s3_file_path = "s3://test/test.jar"
        commands = get_installation_command_for_jar_from_s3(s3_file_path)
        self.assertEqual(
            commands,
            [
                f"aws s3 cp {s3_file_path} {JAVA_PACKAGE_DOWNLOAD_DIR}/test.jar",
            ],
        )

    def get_installation_command_for_txt_from_s3(self):
        s3_file_path = "s3://test/test.txt"
        commands = get_installation_command_for_txt_from_s3(s3_file_path)
        self.assertEqual(
            commands,
            [f"aws s3 cp {s3_file_path} /var/tmp/s3", f"pip install -r /var/tmp/s3/test.txt"],
        )

    def test_get_installation_command_for_wheel_from_s3(self):
        s3_file_path = "s3://test/test.whl"
        commands = get_installation_command_for_wheel_from_s3(s3_file_path)
        self.assertEqual(
            commands,
            [
                f"aws s3 cp {s3_file_path} {PYTHON_PACKAGE_DOWNLOAD_DIR}/test.whl",
                f"pip install {PYTHON_PACKAGE_DOWNLOAD_DIR}/test.whl",
            ],
        )

    def test_get_command_func_for_file_path(self):
        path = "s3://test/test.whl"
        func = get_command_func_for_file_path(path)
        self.assertEqual(func, get_installation_command_for_wheel_from_s3)

        path = "s3://test/test.txt"
        func = get_command_func_for_file_path(path)
        self.assertEqual(func, get_installation_command_for_txt_from_s3)

        path = "s3://test/test.jar"
        func = get_command_func_for_file_path(path)
        self.assertEqual(func, get_installation_command_for_jar_from_s3)

        path = "s3://test/test.zip"
        func = get_command_func_for_file_path(path)
        self.assertEqual(func, get_installation_command_for_zip_from_s3)

        path = "s3://test/test.tar"
        func = get_command_func_for_file_path(path)
        self.assertEqual(func, get_installation_command_for_tar_from_s3)

        path = "s3://test/test.tar.gz"
        func = get_command_func_for_file_path(path)
        self.assertEqual(func, get_installation_command_for_tar_from_s3)

        path = "s3://test/test"
        func = get_command_func_for_file_path(path)
        self.assertIsNone(func)

    def test_get_trusted_host_from_index_url(self):
        index_url = "https://test.com"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test/"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test/test"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test/test/"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test/test/test"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test/test/test/"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test/test/test/test"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test/test/test/test/"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")

        index_url = "http://test.com:8080/test/test/test/test/test"
        trusted_host = get_trusted_host_from_index_url(index_url)
        self.assertEqual(trusted_host, "test.com")
