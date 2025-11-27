from setuptools import setup

package_name = "darwin-mlflow"
version = "2.0.3"

with open("requirements.txt") as f:
    required = f.read().splitlines()

setup(
    name=package_name,
    version=version,
    description="Darwin Mlflow SDK",
    platforms="any",
    packages=["darwin_mlflow", "darwin_mlflow.constant"],
    classifiers=[
        "Programming Language :: Python :: 3.6",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
    ],
    install_requires=required,
    python_requires=">=3.6",
    include_package_data=True,
    zip_safe=False,
    extras_require={"testing": ["mypy>=0.910", "flake8>=3.9"]},
    package_data={
        "darwin_mlflow": ["py.typed"],
    },
)
