# Darwin Compute SDK

### Steps to Release a new version of the SDK
1. Update the version in `setup.py` file

2. Run the commands below to build the SDK
    ```bash
    rm -rf build dist compute_sdk.egg-info
    python setup.py sdist
    ```
