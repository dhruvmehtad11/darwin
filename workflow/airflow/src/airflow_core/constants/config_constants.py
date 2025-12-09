import os

CONFIGS_MAP = {
    "dev": {
        "commuter.configs": {
            "commuter.url": "http://commuter.darwin-d11-stag.local/view",
        },
        "airflow.configs": {
            "airflow.url": "http://mlplatform-airflow-master-stag.dream11-stag.local:8080",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN")}',
        },
        "app-layer-url": f"http://darwin-workflow-mlp-stag4.dream11-stag.local",
        "app-layer-url-public": f"https://darwin-workflow-mlp-stag4.dream11-stag.com",
        "compute-app-layer": f"http://darwin-compute-mlp-stag4.dream11-stag.local/cluster",
        "s3.bucket": "d11-mlstag",
        "DARWIN_URL": f"https://darwin-mlp-stag4.d11dev.com/workflows/",
        "airflow-s3-folder": "darwin_workflow/airflow_artifacts",
        "default_callback_url": f"http://darwin-workflow-mlp-stag4.dream11-stag.local/events",
        "darwin_events_url": f'http://chronos{os.getenv("TEAM_SUFFIX")}.dream11{os.getenv("VPC_SUFFIX")}.local',
        "pelican_url": "http://pelican-orch-gateway-uat.dream11.local",
    },
    "stag": {
        "commuter.configs": {
            "commuter.url": "http://commuter.darwin-d11-stag.local/view",
        },
        "airflow.configs": {
            "airflow.url": "http://mlplatform-airflow-master-stag.dream11-stag.local:8080",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN")}',
        },
        "app-layer-url": f"http://darwin-workflow-mlp-stag4.dream11-stag.local",
        "app-layer-url-public": f"https://darwin-workflow-mlp-stag4.dream11-stag.com",
        "compute-app-layer": f"http://darwin-compute-mlp-stag4.dream11-stag.local/cluster",
        "s3.bucket": "d11-mlstag",
        "DARWIN_URL": f"https://darwin-mlp-stag4.d11dev.com/workflows/",
        "airflow-s3-folder": "darwin_workflow/airflow_artifacts",
        "default_callback_url": f"http://darwin-workflow-mlp-stag4.dream11-stag.local/events",
        "darwin_events_url": f'http://chronos{os.getenv("TEAM_SUFFIX")}.dream11{os.getenv("VPC_SUFFIX")}.local',
        "pelican_url": "http://pelican-orch-gateway-uat.dream11.local",
    },
    "uat": {
        "commuter.configs": {
            "commuter.url": "http://commuter-uat.dream11-k8s.local/view",
        },
        "airflow.configs": {
            "airflow.url": "http://darwin-airflow-master-uat.dream11-k8s.local:8080",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN")}',
        },
        "app-layer-url": "http://darwin-workflow-uat.dream11.local",
        "app-layer-url-public": "https://darwin-workflow-uat.dream11.com",
        "compute-app-layer": "http://darwin-compute-uat.dream11.local/cluster",
        "s3.bucket": "mlplatform-eks-prod",
        "DARWIN_URL": "https://darwin-uat.dream11.com/workflows/",
        "airflow-s3-folder": "darwin_workflow_uat/airflow_artifacts",
        "default_callback_url": "http://darwin-workflow-uat.dream11.local/events",
        "darwin_events_url": "http://chronos-uat.dream11.local",
        "pelican_url": "http://pelican-orch-gateway-uat.dream11.local",
    },
    "prod": {
        "commuter.configs": {
            "commuter.url": "http://commuter-master.dream11-k8s.local/view",
        },
        "airflow.configs": {
            "airflow.url": "http://darwin-master-prod.dream11.local",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN")}',
        },
        "app-layer-url": "http://darwin-workflow.dream11.local",
        "app-layer-url-public": "https://darwin-workflow.dream11.com",
        "compute-app-layer": "http://darwin-compute.dream11.local/cluster",
        "s3.bucket": "mlplatform-eks-prod",
        "DARWIN_URL": "https://darwin.dream11.com/workflows/",
        "airflow-s3-folder": "darwin_workflow/airflow_artifacts",
        "default_callback_url": "http://darwin-workflow.dream11.local/events",
        "darwin_events_url": "http://chronos.dream11.local",
        "pelican_url": "http://pelican-orch-gateway.dream11.local",
    },
}
