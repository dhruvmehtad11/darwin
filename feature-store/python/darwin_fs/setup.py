from setuptools import setup, find_packages

setup(
  name="darwin_fs",
  version="2.0.3",
  packages=find_packages(),
  install_requires=[
    "requests~=2.32.3", "pyyaml~=6.0.2", "dataclasses-json~=0.5.7"
  ],
  extras_require={
    "all": ["pyspark>=3.3.1,<=3.5.4", "aiohttp>=3.7"],
    "spark": ["pyspark>=3.3.1,<=3.5.4"],
    "async": ["aiohttp>=3.7"],
    "test": ["pyspark", "aiohttp>=3.7", "asyncio", "wiremock==2.6.1", "pytest", "pytest-asyncio", "testcontainers==4.9.1", "kafka-python",
             "boto3"]
  },
)