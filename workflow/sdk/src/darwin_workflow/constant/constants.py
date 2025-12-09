import os

CONFIGS_MAP = {
    'local': {
        'workflow_url': f'http://localhost:8000'
        ,"workflow_ui_base_url": 'http://localhost:8000/workflows',
    },
    'stag': {
        'workflow_url': f'http://darwin-workflow{os.getenv("TEAM_SUFFIX")}.dream11{os.getenv("VPC_SUFFIX")}.local'
        ,"workflow_ui_base_url": f'https://darwin{os.getenv("TEAM_SUFFIX")}.d11dev{os.getenv("VPC_SUFFIX")}.com/workflows',
    },
    'uat': {
        'workflow_url': 'http://darwin-workflow-uat.dream11.local'
        ,"workflow_ui_base_url": "https://darwin-uat.dream11.com/workflows",
    },
    'prod': {
        'workflow_url': 'http://darwin-workflow.dream11.local'
        ,"workflow_ui_base_url": "https://darwin.dream11.com/workflows",
    }
}

START_DATE = "2023-06-03T05:55:35Z"