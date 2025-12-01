import io
from setuptools import setup

# Read the long description from README.md
with io.open("README.md", encoding="utf-8") as f:
    long_description = f.read()

# Define the setup configuration
setup(
    name="ipython-autotime",
    version="1.0.4",  # Manually specify the version here
    author="Phillip Cloud",
    author_email="cpcloud@gmail.com",
    description="Time everything in IPython",
    long_description=long_description,
    long_description_content_type="text/markdown",
    license="Apache",
    url="https://github.com/cpcloud/ipython-autotime",
    install_requires=[
        "ipython",
        'monotonic ; python_version < "3.3"',
    ],
    classifiers=[
        "Environment :: Console",
        "Intended Audience :: Developers",
        "Intended Audience :: Science/Research",
        "Intended Audience :: Education",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
        "Programming Language :: Python",
        "Topic :: Scientific/Engineering",
        "Topic :: Utilities",
    ],
    packages=["autotime"],
)
