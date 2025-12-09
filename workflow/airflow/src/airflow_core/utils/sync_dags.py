import subprocess
import time
import logging
import sys
import requests

def api_request(
    method, url, data = None, headers = None
):
    """
    Sends a request to the given URL and returns the response.
    :param method: HTTP method
    :param url: URL
    :param params: Query parameters
    :param data: Request body
    :param headers: Request headers
    :return: Response
    """
    RETRY_COUNT = 0
    while RETRY_COUNT < 3:
        response = requests.request(
            method, url, params=None, json=data, headers=headers
        )
        if not 200 <= response.status_code < 300:
            RETRY_COUNT += 1
        else:
            response_json = response.json()
            return response_json
    raise Exception("failed to send dd")

def send_metric(val):
    """
    Sends a metric data point to Datadog.
    """
    url = "https://api.datadoghq.com/api/v2/series"

    # Fetch API key from environment variable (more secure)
    datadog_api_key = '<dd_token>'
    if not datadog_api_key:
        raise ValueError(
            "Datadog API key not set. Please set the 'DD_API_KEY' environment variable."
        )

    payload = {
        "series": [
            {
                "metric": "aws.ec2.darwin.workflow.sync_dag",
                "tags": [],
                "points": [{"timestamp": int(time.time()), "value": val}],
            }
        ]
    }

    headers = {
        "Accept": "application/json",
        "Content-Type": "application/json",
        "DD-API-KEY": datadog_api_key,
    }

    response = api_request("POST", url, data=payload, headers=headers)
    print("Metric sent successfully:", response)


S3_BUCKET = "s3://mlplatform-eks-prod/darwin_workflow/airflow_artifacts/dags/"
LOCAL_PATH = "/root/airflow/dags/"
SYNC_COMMAND = ["aws", "s3", "sync", S3_BUCKET, LOCAL_PATH, "--exact-timestamps"]
SLEEP_INTERVAL = 1  # seconds
MAX_RETRIES = 3


def sync_s3():
    for attempt in range(1, MAX_RETRIES + 1):
        try:
            print("starting sync")
            process = subprocess.Popen(SYNC_COMMAND, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            stdout, stderr = process.communicate()

            if process.returncode == 0:
                print("S3 Sync Successful")
                logging.info("S3 Sync Successful: %s", stdout.decode())
                send_metric(1)
                return True
            else:
                logging.warning("Attempt %d: S3 Sync Failed: %s", attempt, stderr.decode())
                send_metric(0)

        except OSError as e:  # Handles FileNotFoundError for older Python versions
            logging.critical("AWS CLI not found. Make sure AWS CLI is installed and configured.")
            sys.exit(1)

        time.sleep(2 ** attempt)  # Exponential backoff (2, 4, 8 seconds)

    logging.error("S3 Sync failed after %d attempts. Sending alert!", MAX_RETRIES)
    send_alert()
    return False


def send_alert():
    # Placeholder for alert mechanism (email, Slack, etc.)
    logging.critical("ALERT: S3 Sync failed after multiple retries!")


if __name__ == "__main__":
    while True:
        sync_s3()
        time.sleep(SLEEP_INTERVAL)
