import unittest

from compute_script.util.jupyter_activity import JupyterLabActivity


class TestJupyterActivity(unittest.TestCase):
    def setUp(self):
        self.jupyter_pod_management = JupyterLabActivity(10)

    def test_jupyter_activity(self):
        resp = self.jupyter_pod_management.check_if_active(
            "internal-k8s-istiosys-darwinin-2abac7e4a2-1531789681.us-east-1.elb.amazonaws.com/id-jupyter-18hd1b-jupyter"
        )
        print(resp)
        # self.assertTrue(resp)
