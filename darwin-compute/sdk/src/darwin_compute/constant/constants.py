import os

CONFIGS_MAP = {
    # Use Kubernetes service name when running inside cluster (pods can't access localhost:80)
    # Use localhost/compute when running outside cluster (for local testing)
    "darwin-local": {"compute_url": f'http://darwin-compute:8000'},
}
