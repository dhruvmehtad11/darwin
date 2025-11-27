from typing import Optional

from compute_core.constant.constants import (
    S3_PACKAGE_DOWNLOAD_DIR,
    JAVA_PACKAGE_DOWNLOAD_DIR,
    PYTHON_PACKAGE_DOWNLOAD_DIR,
)


def get_file_name_from_url(url: str):
    return url.split("/")[-1]


def get_command_to_copy_s3_file_to_local(s3_path: str, local_path: str):
    return f"aws s3 cp {s3_path} {local_path}"


def get_unzipped_folder_name_from_zip_file(zip_file_path: str):
    return zip_file_path.replace(".zip", "")


def get_unarchived_folder_name_from_tar_file(tar_file_path: str):
    return tar_file_path.replace(".tar", "").replace(".tar.gz", "")


def get_command_to_extract_and_install_packages_in_dir(dir_path: str):
    return (
        f"for file in {dir_path}/*; do if [[ $file == *.whl ]]; then "
        f"pip install $file; elif [[ $file == *.txt ]]; then "
        f"pip install -r $file; elif [[ $file == *.jar ]]; then "
        f"mkdir -p {JAVA_PACKAGE_DOWNLOAD_DIR} && mv $file {JAVA_PACKAGE_DOWNLOAD_DIR}/$(basename $file); "
        f"fi; done;"
    )


def get_installation_command_for_tar_from_s3(s3_path: str):
    file_name = get_file_name_from_url(s3_path)
    return [
        get_command_to_copy_s3_file_to_local(s3_path, f"{S3_PACKAGE_DOWNLOAD_DIR}/{file_name}"),
        f"tar -xvf {S3_PACKAGE_DOWNLOAD_DIR}/{file_name}",
        get_command_to_extract_and_install_packages_in_dir(
            f"{S3_PACKAGE_DOWNLOAD_DIR}/{get_unarchived_folder_name_from_tar_file(file_name)}"
        ),
    ]


def get_installation_command_for_zip_from_s3(s3_path: str):
    file_name = get_file_name_from_url(s3_path)
    return [
        get_command_to_copy_s3_file_to_local(s3_path, f"{S3_PACKAGE_DOWNLOAD_DIR}/{file_name}"),
        f"unzip {S3_PACKAGE_DOWNLOAD_DIR}/{file_name} -d {S3_PACKAGE_DOWNLOAD_DIR}",
        get_command_to_extract_and_install_packages_in_dir(
            f"{S3_PACKAGE_DOWNLOAD_DIR}/{get_unzipped_folder_name_from_zip_file(file_name)}"
        ),
    ]


def get_installation_command_for_jar_from_s3(s3_path: str):
    file_name = get_file_name_from_url(s3_path)
    return [get_command_to_copy_s3_file_to_local(s3_path, f"{JAVA_PACKAGE_DOWNLOAD_DIR}/{file_name}")]


def get_installation_command_for_txt_from_s3(s3_path: str):
    file_name = get_file_name_from_url(s3_path)
    return [
        get_command_to_copy_s3_file_to_local(s3_path, f"{PYTHON_PACKAGE_DOWNLOAD_DIR}/{file_name}"),
        f"pip install -r {PYTHON_PACKAGE_DOWNLOAD_DIR}/{file_name}",
    ]


def get_installation_command_for_wheel_from_s3(s3_path: str):
    file_name = get_file_name_from_url(s3_path)
    return [
        get_command_to_copy_s3_file_to_local(s3_path, f"{PYTHON_PACKAGE_DOWNLOAD_DIR}/{file_name}"),
        f"pip install {PYTHON_PACKAGE_DOWNLOAD_DIR}/{file_name}",
    ]


def get_command_func_for_file_path(path: str) -> Optional[callable]:
    # Dictionary mapping file extensions to their respective installation command functions
    commands = {
        ".whl": get_installation_command_for_wheel_from_s3,
        ".txt": get_installation_command_for_txt_from_s3,
        ".jar": get_installation_command_for_jar_from_s3,
        ".zip": get_installation_command_for_zip_from_s3,
        ".tar": get_installation_command_for_tar_from_s3,
        ".tar.gz": get_installation_command_for_tar_from_s3,
    }

    # Iterate over the dictionary to find the appropriate function based on the file extension
    for ext, func in commands.items():
        if path.endswith(ext):
            return func

    # Return None if no matching file extension is found
    return None


def get_trusted_host_from_index_url(index_url: str):
    return index_url.split("://")[1].split("/")[0].split(":")[0]
