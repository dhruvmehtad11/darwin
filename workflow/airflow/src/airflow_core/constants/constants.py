from airflow.models import Variable
from typing_extensions import Literal
import os

SLACK_API_URL = "https://slack.com/api/"
SLACK_TOKEN = Variable.get("SLACK_TOKEN", default_var=os.getenv("VAULT_SERVICE_SLACK_TOKEN", ""))
ENV = Variable.get("ENV", default_var=os.getenv("ENV", "local"))
ENV_TYPE = Literal["prod", "stag", "uat", "local"]
SLACK_USERNAME = "prd_ids alert"
FSX_BASE_PATH_DYNAMIC_TRUE = "/home/ray/fsx/workspace/"
WORKSPACE = "workspace"
MAX_ACTIVE_TASKS = 128
FSX_BASE_PATH = "/var/www/fsx/workspace/"
AIRFLOW_LOGS_BASE_PATH = "/root/airflow/fsx/workspace"
DEFAULT_SLACK_CHANNEL = Variable.get("DEFAULT_SLACK_CHANNEL", default_var=os.getenv("DEFAULT_SLACK_CHANNEL", ""))
BASIC = "basic"
JOB = "job"
NUM_HA_CLUSTERS=3
DEFAULT_CRON_TIMEZONE = "IST"
