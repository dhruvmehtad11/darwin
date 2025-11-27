from setuptools import setup, find_packages
import shutil
import os
import sys

package_name = "darwin-compute"
version = "2.0.3"
source_dir = "src"
dest_dir = ""


def prepare_package():
    dest_dir = os.path.join(source_dir, "compute_model")
    src_dir = "../model/src/compute_model"
    if not os.path.exists(dest_dir):
        shutil.copytree(src_dir, dest_dir)
    return dest_dir


def clean_up(dest_dir):
    if os.path.exists(dest_dir):
        shutil.rmtree(dest_dir)


# dest_dir = prepare_package()

if "sdist" in sys.argv:
    dest_dir = prepare_package()

if __name__ == "__main__":
    setup(
        name=package_name,
        version=version,
        description="Darwin Compute SDK",
        platforms="any",
        classifiers=[
            "Programming Language :: Python :: 3.8",
            "Programming Language :: Python :: 3.9",
        ],
        install_requires=[
            "requests~=2.32.3",
            "dataclasses-json~=0.5.7",
            "typeguard~=4.4.2",
            "darwin-logger~=2.0.6",
            "PyYAML~=6.0.2",
        ],
        python_requires=">=3.8",
        include_package_data=True,
        packages=find_packages(where="src"),
        package_dir={
            "darwin_compute": "src/darwin_compute",
            "compute_model": "src/compute_model",
        },
        zip_safe=False,
        extras_require={"testing": ["pytest>=6.0", "pytest-cov>=2.0", "mypy>=0.910", "flake8>=3.9"]},
        package_data={"darwin_compute": ["py.typed"], "compute_model": ["py.typed"]},
    )

try:
    clean_up(dest_dir)
except:
    pass
