from enum import Enum


class PodEvent(Enum):
    # Pod events with values (event_name, severity, should_be_seen_in_ui)
    IMAGE_PULLING = ("Pulling", "INFO", False)
    IMAGE_PULLED = ("Pulled", "INFO", False)
    POD_KILLING = ("Killing", "WARN", True)
    POD_UNHEALTHY = ("Unhealthy", "ERROR", True)
    POD_FAILED = ("Failed", "ERROR", True)
    POD_FAILED_MOUNT = ("FailedMount", "ERROR", False)
    POD_FAILED_SCHEDULING = ("FailedScheduling", "ERROR", True)
    POD_SCHEDULED = ("Scheduled", "INFO", True)
    POD_STARTED = ("Started", "INFO", True)
    POD_CREATED = ("Created", "INFO", True)
    POD_EVICTED = ("Evicted", "WARN", True)
    POD_BACKOFF = ("BackOff", "WARN", True)
    POD_FAILED_CREATE_POD_SANDBOX = ("FailedCreatePodSandBox", "ERROR", False)
    POD_FAILED_SYNC = ("FailedSync", "ERROR", False)
    POD_FAILED_KILL_POD = ("FailedKillPod", "ERROR", False)
    POD_NETWORK_NOT_READY = ("NetworkNotReady", "WARN", False)
    POD_NOMINATED = ("Nominated", "INFO", False)
    POD_SANDBOX_CHANGED = ("SandboxChanged", "INFO", False)
    POD_TAINT_MANAGER_EVICTION = ("TaintManagerEviction", "WARN", False)
    POD_NODE_NOT_READY = ("NodeNotReady", "WARN", False)

    def get_severity(self):
        return self.value[1]

    @classmethod
    def get_event_type(cls, reason):
        return next((event for event in cls if event.value[0] == reason), None)

    @classmethod
    def get_events_for_ui(cls):
        return [event for event in cls if event.value[2]]


class ComputeEvent(Enum):
    CLUSTER_CREATION_REQUEST_RECEIVED = ('CLUSTER_CREATION_REQUEST_RECEIVED', 'INFO', True)
    CLUSTER_CREATED = ('CLUSTER_CREATED', 'INFO', True)
    CLUSTER_CREATION_FAILED = ('CLUSTER_CREATION_FAILED', 'ERROR', True)
    CLUSTER_DELETION_REQUEST_RECEIVED = ('CLUSTER_DELETION_REQUEST_RECEIVED', 'INFO', True)
    CLUSTER_DELETED = ('CLUSTER_DELETED', 'INFO', True)
    CLUSTER_DELETION_FAILED = ('CLUSTER_DELETION_FAILED', 'ERROR', True)
    CLUSTER_UPDATION_REQUEST_RECEIVED = ('CLUSTER_UPDATION_REQUEST_RECEIVED', 'INFO', True)
    CLUSTER_UPDATED = ('CLUSTER_UPDATED', 'INFO', True)
    CLUSTER_UPDATION_FAILED = ('CLUSTER_UPDATION_FAILED', 'ERROR', True)
    CLUSTER_START_REQUEST_RECEIVED = ('CLUSTER_START_REQUEST_RECEIVED', 'INFO', True)
    CLUSTER_START_FAILED = ('CLUSTER_START_FAILED', 'ERROR', True)
    CLUSTER_STOP_REQUEST_RECEIVED = ('CLUSTER_STOP_REQUEST_RECEIVED', 'INFO', True)
    CLUSTER_STOPPED = ('CLUSTER_STOPPED', 'INFO', True)
    CLUSTER_STOP_FAILED = ('CLUSTER_STOP_FAILED', 'ERROR', True)
    CLUSTER_RESTART_REQUEST_RECEIVED = ('CLUSTER_RESTART_REQUEST_RECEIVED', 'INFO', True)
    CLUSTER_RESTART_FAILED = ('CLUSTER_RESTART_FAILED', 'ERROR', True)
    HEAD_NODE_UP = ('HEAD_NODE_UP', 'INFO', True)
    WORKER_NODES_UP = ('WORKER_NODES_UP', 'INFO', True)
    JUPYTER_UP = ('JUPYTER_UP', 'INFO', True)
    CLUSTER_READY = ('CLUSTER_READY', 'INFO', True)
    CLUSTER_TIMEOUT = ('CLUSTER_TIMEOUT', 'WARN', True)
    AUTO_TERMINATED = ('AUTO_TERMINATED', 'WARN', True)
    INIT_SCRIPT_EXECUTION_STARTED = ('INIT_SCRIPT_EXECUTION_STARTED', 'INFO', True)
    INIT_SCRIPT_EXECUTION_SUCCESSFUL = ('INIT_SCRIPT_EXECUTION_SUCCESS', 'INFO', True)
    INIT_SCRIPT_EXECUTION_FAILED = ('INIT_SCRIPT_EXECUTION_FAILED', 'ERROR', True)
    CLUSTER_INACTIVE = ('CLUSTER_INACTIVE', 'WARN', True)

    # Cluster status poller events
    HEAD_NODE_DIED = ('HEAD_NODE_DIED', 'ERROR', True)
    WORKER_NODES_DIED = ('WORKER_NODES_DIED', 'INFO', True)
    CLUSTER_DIED = ('CLUSTER_DIED', 'ERROR', True)
    WORKER_NODES_SCALED = ('WORKER_NODES_SCALED', 'INFO', True)

    # Spark Connect events
    SPARK_CLIENT_INITIATED = ('SPARK_CLIENT_INITIATED', 'INFO', True)
    SPARK_CONNECT_CLIENT_INITIATED = ('SPARK_CONNECT_CLIENT_INITIATED', 'INFO', True)
    SPARK_CONNECT_INITIALIZED = ('SPARK_CONNECT_INITIALIZED', 'INFO', True)
    SPARK_CONNECT_INITIALIZE_FAILED = ('SPARK_CONNECT_INITIALIZE_FAILED', 'ERROR', True)
    SPARK_CONNECT_STOPPED = ('SPARK_CONNECT_STOPPED', 'INFO', True)
    SPARK_CONFIG_FETCH_FAILED = ('SPARK_CONFIG_FETCH_FAILED', 'ERROR', True)
    SPARK_CONNECT_SCRIPT_EXECUTION_STARTED = ('SPARK_CONNECT_SCRIPT_EXECUTION_STARTED', 'INFO', True)
    SPARK_CONNECT_SCRIPT_EXECUTION_FAILED = ('SPARK_CONNECT_SCRIPT_EXECUTION_FAILED', 'ERROR', True)
    SPARK_CONNECT_SCRIPT_EXECUTION_SUCCESSFUL = ('SPARK_CONNECT_SCRIPT_EXECUTION_SUCCESSFUL', 'INFO', True)
    JUPYTER_SPARK_CONNECT_INITIATED = ('JUPYTER_SPARK_CONNECT_INITIATED', 'INFO', True)
    JUPYTER_SPARK_CONNECT_INITIALIZED = ('JUPYTER_SPARK_CONNECT_INITIALIZED', 'INFO', True)
    JUPYTER_SPARK_CONNECT_INITIALIZE_FAILED = ('JUPYTER_SPARK_CONNECT_INITIALIZE_FAILED', 'ERROR', True)
    ORPHAN_CLUSTER_DETECTED = ('ORPHAN_CLUSTER_DETECTED', 'INFO', True)

    def get_severity(self):
        return self.value[1]

    @classmethod
    def get_event_type(cls, reason):
        return next((event for event in cls if event.value[0] == reason), None)

    @classmethod
    def get_events_for_ui(cls):
        return [event for event in cls if event.value[2]]


class AwsEvent(Enum):
    INSTANCE_LAUNCHED = ('INSTANCE_LAUNCHED', 'INFO', True)
    INSTANCE_PENDING = ('pending', 'INFO', True)
    INSTANCE_RUNNING = ('running', 'INFO', True)
    INSTANCE_STOPPING = ('stopping', 'INFO', True)
    INSTANCE_STOPPED = ('stopped', 'INFO', True)
    INSTANCE_SHUTTING_DOWN = ('shutting-down', 'INFO', True)
    INSTANCE_TERMINATED = ('terminated', 'ERROR', True)
    SPOT_INTERRUPTION = ('SPOT_INTERRUPTION', 'WARN', True)

    def get_severity(self):
        return self.value[1]

    @classmethod
    def get_event_type(cls, reason):
        return next((event for event in cls if event.value[0] == reason), None)

    @classmethod
    def get_events_for_ui(cls):
        return [event for event in cls if event.value[2]]


class SparkEvent(Enum):
    ON_NODE_UNEXCLUDED = ("ON_NODE_UNEXCLUDED", "INFO", True)
    ON_NODE_EXCLUDED = ("ON_NODE_EXCLUDED", "INFO", True)
    ON_EXECUTOR_UNEXCLUDED = ("ON_EXECUTOR_UNEXCLUDED", "INFO", True)
    ON_NODE_EXCLUDED_FOR_STAGE = ("ON_NODE_EXCLUDED_FOR_STAGE", "INFO", True)
    ON_EXECUTOR_EXCLUDED_FOR_STAGE = ("ON_EXECUTOR_EXCLUDED_FOR_STAGE", "INFO", True)
    ON_EXECUTOR_EXCLUDED = ("ON_EXECUTOR_EXCLUDED", "INFO", True)
    ON_EXECUTOR_REMOVED = ("ON_EXECUTOR_REMOVED", "INFO", True)
    ON_EXECUTOR_ADDED = ("ON_EXECUTOR_ADDED", "INFO", True)
    ON_APPLICATION_END = ("ON_APPLICATION_END", "INFO", True)
    ON_ENVIRONMENT_UPDATE = ("ON_ENVIRONMENT_UPDATE", "INFO", True)
    ON_JOB_END = ("ON_JOB_END", "INFO", True)
    ON_TASK_GETTING_RESULT = ("ON_TASK_GETTING_RESULT", "INFO", True)
    ON_TASK_START = ("ON_TASK_START", "INFO", True)
    ON_STAGE_SUBMITTED = ("ON_STAGE_SUBMITTED", "INFO", True)
    ON_STAGE_COMPLETED = ("ON_STAGE_COMPLETED", "INFO", True)
    ON_APPLICATION_START = ("ON_APPLICATION_START", "INFO", True)


    @classmethod
    def get_event_type(cls, reason):
        return next((event for event in cls if event.value[0] == reason), None)

    @classmethod
    def get_events_for_ui(cls):
        return [event for event in cls if event.value[2]]