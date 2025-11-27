import re
import subprocess
import time
import unittest

import requests


class TestJupyterlabGit(unittest.TestCase):
    def setUp(self):
        self.jupyter_process = subprocess.Popen(
            ["jupyter", "lab", "--no-browser", "--port=8888", "--ServerApp.token=test"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        self.headers = {"Authorization": "token " + "test"}
        time.sleep(5)

    def test_jupyterlab_git_extension_enabled(self):
        try:
            result = subprocess.run(
                ["jupyter", "labextension", "list"],
                text=True,
                check=True,
                capture_output=True,
            )

            output = re.sub(r"\x1b\[[0-9;]*m", "", result.stderr)

            self.assertIn(
                "enabled OK (python, jupyterlab-git)", output, "The '@jupyterlab/git' extension is not enabled."
            )
        except subprocess.CalledProcessError as e:
            self.fail(f"Failed to run 'jupyter labextension list': {e}")
        except Exception as e:
            self.fail(f"An unexpected error occurred: {e}")

    def test_jupyterlab_git(self):
        resp = requests.post("http://localhost:8888/git/branch", headers=self.headers)
        assert resp.status_code == 200

    def tearDown(self):
        self.jupyter_process.terminate()
        self.jupyter_process.wait()
