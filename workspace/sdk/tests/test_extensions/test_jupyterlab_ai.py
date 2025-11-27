import re
import subprocess
import time
import unittest

import requests


class TestJupyterLabAI(unittest.TestCase):
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

    def test_jupyterlab_ai_extension_enabled(self):
        try:
            result = subprocess.run(
                ["jupyter", "labextension", "list"],
                text=True,
                check=True,
                capture_output=True,
            )

            output = re.sub(r"\x1b\[[0-9;]*m", "", result.stderr)

            self.assertIn("enabled OK (python, jupyter_ai)", output, "The '@jupyter-ai/core' extension is not enabled.")
        except subprocess.CalledProcessError as e:
            self.fail(f"Failed to run 'jupyter labextension list': {e}")
        except Exception as e:
            self.fail(f"An unexpected error occurred: {e}")

    def test_jupyterlab_ai_api_config(self):
        resp = requests.get("http://localhost:8888/api/ai/config", headers=self.headers)
        assert resp.status_code == 200

    def test_jupyterlab_ai_api_providers(self):
        resp = requests.get("http://localhost:8888/api/ai/providers", headers=self.headers)
        assert resp.status_code == 200

    def test_jupyterlab_ai_api_embeddings(self):
        resp = requests.get("http://localhost:8888/api/ai/providers/embeddings", headers=self.headers)
        assert resp.status_code == 200

    def tearDown(self):
        self.jupyter_process.terminate()
        self.jupyter_process.wait()
