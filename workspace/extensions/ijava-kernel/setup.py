from setuptools import setup, find_packages

setup(
    name="ijava-kernel",
    version="1.0.0",
    packages=find_packages(),
    include_package_data=True,  # Ensure non-Python files are included
    package_data={
        "ijava_kernel": ["java/**"],  # Include all files inside java/
    },
    install_requires=["jupyter_client"],
    entry_points={
        "console_scripts": [
            "ijava-install=ijava_kernel.install:main",
        ],
    },
)
