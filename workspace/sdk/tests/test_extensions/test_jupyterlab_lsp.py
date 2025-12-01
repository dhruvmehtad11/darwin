import re
import subprocess
import time
import unittest


class TestJupyterlabLsp(unittest.TestCase):
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

    def test_jupyterlab_lsp_extension_enabled(self):
        try:
            result = subprocess.run(
                ["jupyter", "labextension", "list"],
                text=True,
                check=True,
                capture_output=True,
            )

            output = re.sub(r"\x1b\[[0-9;]*m", "", result.stderr)

            self.assertIn(
                "enabled OK (python, jupyterlab-lsp)",
                output,
                "The '@jupyter-lsp/jupyterlab-lsp' extension is not enabled.",
            )
        except subprocess.CalledProcessError as e:
            self.fail(f"Failed to run 'jupyter labextension list': {e}")
        except Exception as e:
            self.fail(f"An unexpected error occurred: {e}")

    def tearDown(self):
        self.jupyter_process.terminate()
        self.jupyter_process.wait()
