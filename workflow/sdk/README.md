# Darwin Workflow SDK

### Steps to Release a new version of the SDK
1. Update the version in `setup.py` file


2. Run the commands below to build the SDK
```
rm -rf build dist workflow_sdk.egg-info
python setup.py sdist
```

3. Install twine if not installed
```
pip3 install twine
```

4. Run the command below to upload the SDK to PyPi
```
twine upload --repository-url <pypi-server-link> dist/*
```
stag pypi-server-link = http://pypi-server.darwin-d11-stag.local/

prod pypi-server-link = http://pypi-server.darwin.dream11-k8s.local/

If username and password is asked, enter blank for both.

**Note** - You will need to be connected for VPN for this