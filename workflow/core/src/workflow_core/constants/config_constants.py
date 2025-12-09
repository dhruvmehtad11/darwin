import os

CONFIGS_MAP = {
    "darwin-local": {
        "dataset.configs": {
            "elastic-search.url": "http://darwin-elasticsearch:9200",
            "elastic-search.user": f'{os.getenv("VAULT_SERVICE_ES_USERNAME", "darwin")}',
            "elastic-search.pwd": f'{os.getenv("VAULT_SERVICE_ES_PASSWORD", "local-password")}'
        },
        "airflow.configs": {
            "airflow.url": "http://darwin-airflow-webserver:8080",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN", "Basic_YWRtaW46YWRtaW5AMTIz")}'
        },
        "commuter.configs": {
            "commuter.url": "http://darwin-commuter.local/view"
        },
        "compute.configs": {
            "compute.url": "http://darwin-compute.local"
        },
        "airflow_mysql_db": {
            'host': f'{os.getenv("VAULT_SERVICE_WORKFLOW_HOST", "darwin-mysql")}',
            'username': f'{os.getenv("VAULT_SERVICE_MYSQL_USERNAME", "root")}',
            'password': f'{os.getenv("VAULT_SERVICE_MYSQL_PASSWORD", "password")}',
            'database': f'{os.getenv("CONFIG_SERVICE_MYSQL_DATABASE", "darwin_workflow")}',
            'port': 3306
        },
        "workflow_db": {
            'host': f'{os.getenv("VAULT_SERVICE_WORKFLOW_HOST", "darwin-mysql")}',
            'username': f'{os.getenv("VAULT_SERVICE_WORKFLOW_USERNAME", "root")}',
            'password': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PASSWORD", "password")}',
            'database': f'{os.getenv("VAULT_SERVICE_WORKFLOW_DATABASE", "darwin_workflow")}',
            'port': int(os.getenv("VAULT_SERVICE_WORKFLOW_PORT", "3306"))
        },
        "app-layer-url": "http://darwin-workflow:8000",
        "app-layer-url-public": "http://localhost/workflow",
        "compute-app-layer": "http://darwin-compute.local/cluster",
        "s3.bucket": "darwin-local-bucket",
        "DARWIN_URL": "http://localhost/workflow/workflows/",
        "airflow-s3-folder": "darwin_workflow_local/airflow_artifacts",
        "default_callback_url": "http://darwin-workflow:8000/events",
        "darwin_events_url": "http://darwin-events.local"
    },

    "dev": {
        "dataset.configs": {
            "elastic-search.url": "https://vpc-darwin-metadata-store-tx5nu3gsm6qvxeo3draktmuoei.us-east-1.es.amazonaws.com:443/",
            "elastic-search.user": f'{os.getenv("VAULT_SERVICE_ES_USERNAME")}',
            "elastic-search.pwd": f'{os.getenv("VAULT_SERVICE_ES_PASSWORD")}'
        },
        "airflow.configs": {
            "airflow.url": "http://mlplatform-airflow-master-stag.dream11-stag.local:8080",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN")}'
        },
        "commuter.configs": {
            "commuter.url": "http://commuter.darwin-d11-stag.local/view"
        },
        "compute.configs": {
            "compute.url": f"http://darwin-compute{os.getenv('TEAM_SUFFIX')}.dream11-stag.local"
        },
        "airflow_mysql_db": {
            'host': 'darwin-airflow.cluster-ro-ci4w7bztwisc.us-east-1.rds.amazonaws.com',
            'username': f'{os.getenv("VAULT_SERVICE_MYSQL_USERNAME")}',
            'password': f'{os.getenv("VAULT_SERVICE_MYSQL_PASSWORD")}',
            'database': 'mlplatform',
            'port': 3306
        },
        "workflow_db": {
            'host': f'{os.getenv("VAULT_SERVICE_WORKFLOW_HOST")}',
            'username': f'{os.getenv("VAULT_SERVICE_WORKFLOW_USERNAME")}',
            'password': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PASSWORD")}',
            'database': f'{os.getenv("VAULT_SERVICE_WORKFLOW_DATABASE")}',
            'port': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PORT")}'
        },
        "app-layer-url": f"http://darwin-workflow{os.getenv('TEAM_SUFFIX')}.dream11-stag.local",
        "app-layer-url-public": f"https://darwin-workflow{os.getenv('TEAM_SUFFIX')}.dream11-stag.com",
        "compute-app-layer": f"http://darwin-compute{os.getenv('TEAM_SUFFIX')}.dream11-stag.local/cluster",
        "s3.bucket": "d11-mlstag",
        "DARWIN_URL": f"https://darwin{os.getenv('TEAM_SUFFIX')}.d11dev.com/workflows/",
        "airflow-s3-folder": "darwin_workflow/airflow_artifacts",
        "default_callback_url": f"http://darwin-workflow{os.getenv('TEAM_SUFFIX')}.dream11-stag.local/events",
        'darwin_events_url': f'http://chronos{os.getenv("TEAM_SUFFIX")}.dream11{os.getenv("VPC_SUFFIX")}.local'
    },

    "stag": {
        "dataset.configs": {
            "elastic-search.url": "https://vpc-darwin-metadata-store-tx5nu3gsm6qvxeo3draktmuoei.us-east-1.es.amazonaws.com:443/",
            "elastic-search.user": f'{os.getenv("VAULT_SERVICE_ES_USERNAME")}',
            "elastic-search.pwd": f'{os.getenv("VAULT_SERVICE_ES_PASSWORD")}'
        },
        "airflow.configs": {
            "airflow.url": "http://mlplatform-airflow-master-stag.dream11-stag.local:8080",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN")}'
        },
        "commuter.configs": {
            "commuter.url": "http://commuter.darwin-d11-stag.local/view"
        },
        "compute.configs": {
            "compute.url": f"http://darwin-compute{os.getenv('TEAM_SUFFIX')}.dream11-stag.local"
        },
        "airflow_mysql_db": {
            'host': 'darwin-airflow.cluster-ro-ci4w7bztwisc.us-east-1.rds.amazonaws.com',
            'username': f'{os.getenv("VAULT_SERVICE_MYSQL_USERNAME")}',
            'password': f'{os.getenv("VAULT_SERVICE_MYSQL_PASSWORD")}',
            'database': 'mlplatform',
            'port': 3306
        },
        "workflow_db": {
            'host': f'{os.getenv("VAULT_SERVICE_WORKFLOW_HOST")}',
            'username': f'{os.getenv("VAULT_SERVICE_WORKFLOW_USERNAME")}',
            'password': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PASSWORD")}',
            'database': f'{os.getenv("VAULT_SERVICE_WORKFLOW_DATABASE")}',
            'port': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PORT")}'
        },
        "app-layer-url": f"http://darwin-workflow{os.getenv('TEAM_SUFFIX')}.dream11-stag.local",
        "app-layer-url-public": f"https://darwin-workflow{os.getenv('TEAM_SUFFIX')}.dream11-stag.com",
        "compute-app-layer": f"http://darwin-compute{os.getenv('TEAM_SUFFIX')}.dream11-stag.local/cluster",
        "s3.bucket": "d11-mlstag",
        "DARWIN_URL": f"https://darwin{os.getenv('TEAM_SUFFIX')}.d11dev.com/workflows/",
        "airflow-s3-folder": "darwin_workflow/airflow_artifacts",
        "default_callback_url": f"http://darwin-workflow{os.getenv('TEAM_SUFFIX')}.dream11-stag.local/events",
        "darwin_events_url": f'http://chronos{os.getenv("TEAM_SUFFIX")}.dream11{os.getenv("VPC_SUFFIX")}.local'
    },

    "uat": {
        "dataset.configs": {
            'elastic-search.url': 'https://vpc-darwin-workflow-uat-194-iwxmudpymh335qxbtmh744kimy.us-east-1.es.amazonaws.com:443/',
            "elastic-search.user": f'{os.getenv("VAULT_SERVICE_ES_USERNAME")}',
            "elastic-search.pwd": f'{os.getenv("VAULT_SERVICE_ES_PASSWORD")}'
        },
        "airflow.configs": {
            "airflow.url": "http://darwin-airflow-master-uat.dream11-k8s.local:8080",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN")}'
        },
        "commuter.configs": {
            "commuter.url": "http://commuter-uat.dream11-k8s.local/view"
        },
        "compute.configs": {
            "compute.url": "http://darwin-compute-uat.dream11.local"
        },
        "airflow_mysql_db": {
            'host': 'darwin-airflow.cluster-ro-c9lfs3lvwg4y.us-east-1.rds.amazonaws.com',
            'username': f'{os.getenv("VAULT_SERVICE_MYSQL_USERNAME")}',
            'password': f'{os.getenv("VAULT_SERVICE_MYSQL_PASSWORD")}',
            'database': f'{os.getenv("CONFIG_SERVICE_MYSQL_DATABASE")}',
            'port': 3306
        },
        "workflow_db": {
            'host': f'{os.getenv("VAULT_SERVICE_WORKFLOW_HOST")}',
            'username': f'{os.getenv("VAULT_SERVICE_WORKFLOW_USERNAME")}',
            'password': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PASSWORD")}',
            'database': f'{os.getenv("VAULT_SERVICE_WORKFLOW_DATABASE")}',
            'port': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PORT")}'
        },
        "app-layer-url": "http://darwin-workflow-uat.dream11.local",
        "app-layer-url-public": "https://darwin-workflow-uat.dream11.com",
        "compute-app-layer": "http://darwin-compute-uat.dream11.local/cluster",
        "s3.bucket": "mlplatform-eks-prod",
        "DARWIN_URL": "https://darwin-uat.dream11.com/workflows/",
        "airflow-s3-folder": "darwin_workflow_uat/airflow_artifacts",
        "default_callback_url": "http://darwin-workflow-uat.dream11.local/events",
        "darwin_events_url": "http://chronos-uat.dream11.local"
    },
    "prod": {
        "dataset.configs": {
            'elastic-search.url': 'https://vpc-darwin-cluster-manager-c46x4jbspqdxk4dvbuhkcwb7ri.us-east-1.es.amazonaws.com:443/',
            "elastic-search.user": f'{os.getenv("VAULT_SERVICE_ES_USERNAME")}',
            "elastic-search.pwd": f'{os.getenv("VAULT_SERVICE_ES_PASSWORD")}'
        },
        "airflow.configs": {
            "airflow.url": "http://darwin-master-prod.dream11.local",
            "airflow.auth": f'{os.getenv("VAULT_SERVICE_AIRFLOW_TOKEN")}'
        },
        "commuter.configs": {
            "commuter.url": "http://commuter-master.dream11-k8s.local/view"
        },
        "compute.configs": {
            "compute.url": "http://darwin-compute.dream11.local"
        },
        "airflow_mysql_db": {
            'host': 'darwin-airflow.cluster-ro-c9lfs3lvwg4y.us-east-1.rds.amazonaws.com',
            'username': f'{os.getenv("VAULT_SERVICE_MYSQL_USERNAME")}',
            'password': f'{os.getenv("VAULT_SERVICE_MYSQL_PASSWORD")}',
            'database': f'{os.getenv("CONFIG_SERVICE_MYSQL_DATABASE")}',
            'port': 3306
        },
        "workflow_db": {
            'host': f'{os.getenv("VAULT_SERVICE_WORKFLOW_HOST")}',
            'username': f'{os.getenv("VAULT_SERVICE_WORKFLOW_USERNAME")}',
            'password': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PASSWORD")}',
            'database': f'{os.getenv("VAULT_SERVICE_WORKFLOW_DATABASE")}',
            'port': f'{os.getenv("VAULT_SERVICE_WORKFLOW_PORT")}'
        },
        "app-layer-url": "http://darwin-workflow.dream11.local",
        "app-layer-url-public": "https://darwin-workflow.dream11.com",
        "compute-app-layer": "http://darwin-compute.dream11.local/cluster",
        "s3.bucket": "mlplatform-eks-prod",
        "DARWIN_URL": "https://darwin.dream11.com/workflows/",
        "airflow-s3-folder": "darwin_workflow/airflow_artifacts",
        "default_callback_url": "http://darwin-workflow.dream11.local/events",
        "darwin_events_url": "http://chronos.dream11.local"
    }
}
