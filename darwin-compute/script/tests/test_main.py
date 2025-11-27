import os

from compute_core.compute import Compute
from compute_script.main import manage_jupyter_pods
from compute_core.constant.config import Config


def test_manage_jupyter_pods():
    ENV = os.getenv("ENV", "stag")
    compute = Compute(ENV)
    config = Config(ENV)
    manage_jupyter_pods(ENV, compute, config)
