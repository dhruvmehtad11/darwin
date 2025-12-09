import datetime
import os
from abc import ABC
from datetime import timedelta
from airflow import DAG
from airflow.models import Variable
from airflow.models.baseoperator import chain
from airflow.operators.python import PythonOperator
from operators.pelican_operator import PelicanOperator
from operators.darwin_operator import DarwinOperator
from airflow.timetables.simple import (
    OnceTimetable, NullTimetable
)
from airflow.timetables.trigger import CronTriggerTimetable
# Changed from compute_sdk to darwin-compute: The package name 'compute_sdk' doesn't exist.
# The actual package is 'darwin-compute' (importable as 'darwin_compute') from darwin-compute/sdk.
# This fixes the "No module named 'compute_sdk'" error in Airflow plugins.
from darwin_compute.compute import ComputeCluster

from airflow_core.constants.constants import MAX_ACTIVE_TASKS, DEFAULT_CRON_TIMEZONE
from airflow_core.jobs.airflow_job_runner import AirflowJobRunner
from airflow_core.utils.airflow_job_runner_utils import get_date, pre_execute, post_execute, \
    check_run_duration, pre_execute_v3, post_execute_v3


def delete_slack_thread_id_and_stop_cluster(context):
    task_id = context['task_instance'].task_id
    dag_id = context['task_instance'].dag_id
    run_id = context['run_id']
    cluster_id = None

    try:
        key1 = f"cluster_id_{dag_id}_{task_id}_{run_id}"
        cluster_id = Variable.get(key1)
        compute_sdk = ComputeCluster(Variable.get("ENV"))
        cluster_details = compute_sdk.get_details(cluster_id)['data']
        if cluster_details['status'] != "inactive" and cluster_details['is_job_cluster']:
            compute_sdk.stop(cluster_id)

        Variable.delete(key1)
    except Exception as e:
        if cluster_id:
            print(f"Error stopping cluster {cluster_id}: {e}")
        else:
            print(f"Error stopping cluster: {e}")

    try:
        key2 = f"slack_thread_id_{dag_id}_{task_id}_{run_id}"
        Variable.delete(key2)
    except Exception as e:
        print(f"Error deleting slack thread id: {e}")


class Artefact(ABC):
    """
        Base class for Artefact template
    """

    def __init__(self, json):
        self.task_definitions = json["tasks_definitions"]
        self.task_dependencies = json["tasks_dependencies"]
        self.tasks_dict = {}
        self.task_map = {}
        self.dag = None
        self.jobs_sdk = AirflowJobRunner(json['env'])

    def create_dag(self, json):
        dag_id = json['dag_args']['dag_id']
        dag_args_timetable = json['dag_args'].get('timetable')
        if dag_args_timetable == '@once':
            timetable = OnceTimetable()
        elif dag_args_timetable is 'None':
            timetable = NullTimetable()
        else:
            # Validate the cron expression; set timezone
            timezone = json['dag_args'].get('timezone', DEFAULT_CRON_TIMEZONE)
            timetable = CronTriggerTimetable(dag_args_timetable,
                                             timezone="Asia/Kolkata" if timezone == "IST" else "UTC")
        tags = json['dag_args']['tags']
        retries = json['dag_args']['retries']
        max_active_runs = json['dag_args']['concurrency']
        max_active_tasks = MAX_ACTIVE_TASKS
        default_args = {
            'depends_on_past': False,
            'email': ['airflow@example.com'],
            'email_on_failure': False,
            'email_on_retry': False,
            'retries': retries,
            'retry_exponential_backoff': True,
            'retry_delay': timedelta(seconds=10),
            'max_retry_delay': timedelta(minutes=2),
        }
        start_date = get_date(json['dag_args'].get('start_date')) or datetime.datetime(2024, 1, 1)
        end_date = get_date(json['dag_args'].get('end_date'))
        self.dag = DAG(dag_id=dag_id, start_date=start_date, end_date=end_date, default_args=default_args,
                       timetable=timetable, max_active_runs=max_active_runs, max_active_tasks=max_active_tasks,
                       tags=tags, catchup=json['dag_args'].get('queue_enabled'),
                       on_failure_callback=delete_slack_thread_id_and_stop_cluster,
                       on_success_callback=delete_slack_thread_id_and_stop_cluster)
        return self.dag

    def create_operator(self, json):
        """
        Create Airflow Operators from the task definitions.
        :param json: JSON data containing task definitions
        :return: None
        """
        for task_def in json['tasks_definitions']:
            timeout = task_def.get('op_kwargs', {}).get('timeout', 36000)
            retries = task_def.get('op_kwargs', {}).get('retries', 1)
            task_id = task_def["task_id"]
            dag_id = json["dag_args"]["dag_id"]
            task_type = task_def.get('op_kwargs', {}).get('task_type', '').lower()

            task_def['op_kwargs']['job_id'] = "{{dag_run.run_id}}"
            task_def['op_kwargs']['dag_id'] = dag_id
            task_def['op_kwargs']['task_id'] = task_id
            task_def['op_kwargs']['user_email'] = json['dag_args']['created_by']
            task_def['op_kwargs']['notify_on'] = json['dag_args']['notify_on']

            if task_type == 'pelican':
                pelican_kwargs = task_def['op_kwargs']
                t = PelicanOperator(
                    task_id=task_id,
                    pelican_gateway_host=pelican_kwargs.get('pelican_gateway_host', 'http://pelican-orch-gateway-uat.dream11.local'),
                    artifact=pelican_kwargs['artifact'],
                    task_name=pelican_kwargs['task_name'],
                    poll_interval=pelican_kwargs.get('poll_interval', pelican_kwargs.get('polling_interval', 15)),
                    timeout=pelican_kwargs.get('timeout', 3600),
                    max_http_retries=pelican_kwargs.get('max_http_retries', 3),
                    max_retries=pelican_kwargs.get('max_retries', retries),
                    compute_cluster_config=pelican_kwargs.get('compute_cluster_config'),
                    engine_config=pelican_kwargs.get('engine_config'),
                    instance_role=pelican_kwargs.get('instance_role'),
                    dag=self.dag,
                    retries=retries,
                    pool=json["dag_args"]["tenant"]
                )
            else:
                entry_point_cmd = task_def.get('op_kwargs', {}).get('entry_point_cmd', '')
                if entry_point_cmd.startswith('papermill'):
                    args = entry_point_cmd.split()
                    output_path = args[2]

                    # Split the output path into directory and filename parts
                    output_dir, output_filename = os.path.split(output_path)

                    # Modify only the filename part (keeping the directory unchanged)
                    filename_parts = os.path.splitext(output_filename)
                    new_output_filename = filename_parts[0] + "/" + "{{dag_run.run_id}}" + filename_parts[1]

                    # Join the modified filename with the directory path
                    new_output_path = os.path.join(output_dir, new_output_filename)

                    args[2] = new_output_path
                    new_output_cmd = " ".join(args)
                    task_def['op_kwargs']['entry_point_cmd'] = new_output_cmd
                task_def['op_kwargs']['runtime_params'] = "{{dag_run.conf}}"
                darwin_kwargs = task_def['op_kwargs']
                t = DarwinOperator(
                    task_id=task_id,
                    cluster_id=darwin_kwargs['cluster_id'],
                    entry_point_cmd=darwin_kwargs['entry_point_cmd'],
                    cluster_type=darwin_kwargs.get('cluster_type', 'job'),
                    time_out_in_sec_for_cluster=darwin_kwargs.get('time_out_in_sec_for_cluster', 12000),
                    time_out_in_sec_for_job=darwin_kwargs.get('time_out_in_sec_for_job', 360000),
                    wait_time_for_shutdown_in_sec=darwin_kwargs.get('wait_time_for_shutdown_in_sec', 600),
                    runtime_env=darwin_kwargs.get('runtime_env'),
                    notify_on=darwin_kwargs.get('notify_on', 'darwin-workflow-alerts'),
                    default_params=darwin_kwargs.get('default_params'),
                    runtime_params=darwin_kwargs.get('runtime_params'),
                    workflow_params=darwin_kwargs.get('workflow_params'),
                    user_email=darwin_kwargs.get('user_email'),
                    kill_cluster=darwin_kwargs.get('kill_cluster', True),
                    packages=darwin_kwargs.get('packages', []),
                    ha_config=darwin_kwargs.get('ha_config', {}),
                    dag=self.dag,
                    retries=retries,
                    pool=json["dag_args"]["tenant"]
                )

            self.task_map[task_def.get('task_id')] = t

    def create_dependencies(self, dag_json):
        """Create the dependencies for the DAG by chaining the tasks together.
        :param dag_json: The JSON representation of the DAG
        :raises Exception: if there is an error creating the dependencies
        """
        for task1, task2 in dag_json["tasks_dependencies"]:
            task1 = self.task_map[task1]
            task2 = self.task_map[task2]
            chain(task1, task2)


    def add_pre_post_monitoring_tasks(self, json):
        """
        Dynamically injects pre, post, and monitoring tasks
        """
        # Define Pre-execution Task
        pre_task = PythonOperator(
            task_id="pre_execute",
            python_callable=pre_execute_v3,
            op_kwargs={"expected_run_duration": json['dag_args']['expected_run_duration']},
            dag=self.dag,
            retries=0,
            pool=json["dag_args"]["tenant"]
        )

        # Define Post-execution Task
        post_task = PythonOperator(
            task_id="post_execute",
            python_callable=post_execute_v3,
            op_kwargs=json['dag_args'],
            dag=self.dag,
            trigger_rule="all_done",
            retries=0,
            pool=json["dag_args"]["tenant"]
        )

        # Define Monitoring Task (Parallel)
        duration_monitor = PythonOperator(
            task_id="check_run_duration",
            python_callable=check_run_duration,
            op_kwargs={"expected_run_duration": json['dag_args']['expected_run_duration']},
            provide_context=True,
            dag=self.dag,
            retries=0,
            pool = json["dag_args"]["tenant"]
        )

        # Identify start and end tasks dynamically
        all_tasks = list(self.task_map.values())
        start_tasks = [t for t in all_tasks if not t.upstream_list]  # No upstream dependencies
        end_tasks = [t for t in all_tasks if not t.downstream_list]  # No downstream dependencies

        # Connect pre_task to all start tasks
        for task in start_tasks:
            pre_task >> task

        # Connect all end tasks to post_task
        for task in end_tasks:
            task >> post_task

        # Run monitoring task in parallel
        pre_task >> duration_monitor


# Create DAG
airflow_artefact = Artefact(json)
dag = airflow_artefact.create_dag(json)
airflow_artefact.create_operator(json)
airflow_artefact.create_dependencies(json)
airflow_artefact.add_pre_post_monitoring_tasks(json)

# Register DAG
globals()[json['dag_args']['dag_id']] = dag 