import subprocess
import pkg_resources


def generate_core_components():
    markdown = "## Core Components\n\n"
    markdown += "| Component | Version |\n"
    markdown += "| --- | --- |\n"
    import sys

    py_version = sys.version
    markdown += f"| python | {py_version} |\n"
    import pyspark

    pyspark_version = pyspark.__version__
    markdown += f"| pyspark | {pyspark_version} |\n"
    try:
        ray_version = subprocess.check_output(["ray", "--version"]).decode("utf-8").strip()
    except subprocess.CalledProcessError:
        output = "Not installed"
    markdown += f"| ray | {ray_version} |\n"
    try:
        java_version = subprocess.check_output(["java", "-version"], stderr=subprocess.STDOUT).decode("utf-8").strip()
    except subprocess.CalledProcessError:
        output = "Not installed"
    markdown += f"| java | {java_version} |\n"
    markdown += "\n"
    return markdown


def generate_darwin_libraries():
    libraries = [{"Library": "darwin sdk", "supported": "yes"}]
    markdown = "## Darwin Libraries\n\n"
    markdown += "| Library | supported |\n"
    markdown += "| --- | --- |\n"
    for library in libraries:
        markdown += f"| {library['Library']} | {library['supported']} |\n"
    markdown += "\n"
    return markdown


def generate_important_libraries(important_libs):
    markdown = "## Important Libraries\n\n"
    markdown += "| Library | Version |\n"
    markdown += "| --- | --- |\n"

    installed_packages = {pkg.key: pkg.version for pkg in pkg_resources.working_set}
    for library in important_libs:
        version = installed_packages.get(library, "Not installed")
        markdown += f"| {library} | {version} |\n"
    markdown += "\n"
    return markdown


def generate_full_library_list():
    output = subprocess.check_output(["pip", "list"]).decode("utf-8")
    markdown = "## Full library list\n\n"
    markdown += f"```\n{output}\n```"
    return markdown


def generate_markdown_file(important_libs):
    markdown = generate_core_components()
    markdown += generate_darwin_libraries()
    markdown += generate_important_libraries(important_libs)
    markdown += generate_full_library_list()

    with open("./gitbook_page.md", "w") as file:
        file.write(markdown)


important_libraries = [
    "torch",
    "xgboost",
    "xgboost-ray",
    "tensorflow",
    "pytorch-lightning",
    "raydp",
    "lightgbm",
    "lightgbm_ray",
    "scikit-learn",
    "keras",
    "dask",
    "modin",
    "numpy",
    "pandas",
    "pyarrow",
]

generate_markdown_file(important_libraries)
