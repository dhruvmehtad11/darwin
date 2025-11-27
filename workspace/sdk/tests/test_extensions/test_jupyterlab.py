import subprocess
import time
from unittest import TestCase

import requests


class TestJupyterlab(TestCase):
    def setUp(self):
        self.jupyter_process = subprocess.Popen(
            [
                "jupyter",
                "lab",
                "--no-browser",
                "--port=8888",
                "--AiExtension.default_language_model=bedrock-chat:anthropic.claude-v2",
                "--ServerApp.token=test",
            ],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        self.headers = {"Authorization": "token " + "test"}
        time.sleep(5)

    def test_jupyterlab(self):
        resp = requests.get("http://localhost:8888", headers=self.headers)
        assert resp.status_code == 200

    def test_jupyterlab_kernel(self):
        resp = requests.get("http://localhost:8888/api/kernels", headers=self.headers)
        assert resp.status_code == 200

    def test_jupyterlab_terminal(self):
        resp = requests.get("http://localhost:8888/api/terminals", headers=self.headers)
        assert resp.status_code == 200

    def tearDown(self):
        self.jupyter_process.terminate()
        self.jupyter_process.wait()
