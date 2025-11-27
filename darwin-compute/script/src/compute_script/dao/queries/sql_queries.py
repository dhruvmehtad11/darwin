GET_CLUSTER = """
SELECT *
FROM cluster_status
WHERE status != 'inactive'
  AND last_picked_at <= NOW() - INTERVAL '10' second
ORDER BY last_updated_at
LIMIT 1
FOR
UPDATE SKIP LOCKED;
"""

UPDATE_CLUSTER_LAST_PICKED_AT = """
UPDATE cluster_status
SET last_picked_at = NOW()
WHERE cluster_id = %(cluster_id)s;
"""

UPDATE_CLUSTER_LAST_UPDATED_AT = """
UPDATE cluster_status
SET last_updated_at = NOW()
WHERE cluster_id = %(cluster_id)s;
"""

UPDATE_CLUSTER_LAST_USED_AT = """
UPDATE cluster_status
SET last_used_at = NOW()
WHERE cluster_id = %(cluster_id)s;
"""

GET_CLUSTER_ACTION_TIME = """
SELECT updated_at
FROM cluster_actions
WHERE cluster_runid = %(run_id)s
  AND action = %(cluster_action)s
ORDER BY updated_at DESC
LIMIT 1;
"""

"""
This query returns the most recent `updated_at` timestamp for a specific cluster action (`cluster_action`) 
within a given `cluster_runid` (`run_id`) along with `updated_at` timestamp for a 
different action occurring just after the latest `updated_at` for the specified `cluster_action`.
"""
GET_CLUSTER_ACTION_LATEST_AND_ENDING_TIME = """
WITH latest_action_time AS (
    SELECT MAX(`updated_at`) AS `latest_time`
    FROM `cluster_actions`
    WHERE `action` =  %(cluster_action)s
      AND `cluster_runid` = %(run_id)s
),
latest_ending_time AS (
    SELECT `updated_at`
    FROM `cluster_actions`
    WHERE `cluster_runid` = %(run_id)s
      AND `updated_at` >(SELECT `latest_time` FROM latest_action_time)
    ORDER BY `updated_at`
    LIMIT 1
)
SELECT
    (SELECT `latest_time` FROM latest_action_time) AS `latest_action_time`,
    (SELECT `updated_at` FROM latest_ending_time) AS `latest_ending_time`;
"""

GET_RUNNING_REMOTE_COMMANDS = """
SELECT cluster_id, execution_id, target, status
FROM remote_command_status
WHERE status = 'running' AND last_picked_at <= NOW() - INTERVAL '10' second
ORDER BY updated_at
LIMIT 2
FOR
UPDATE SKIP LOCKED;
"""

UPDATE_REMOTE_COMMANDS_LAST_PICKED_AT = """
UPDATE remote_command_status
SET last_picked_at = NOW()
WHERE cluster_id = %(cluster_id)s and execution_id = %(execution_id)s;
"""

GET_PODS_COMMAND_EXECUTION_STATUS = """
SELECT cluster_run_id, execution_id, pod_name, status
FROM pod_command_execution_status
WHERE cluster_run_id = %(cluster_run_id)s
AND execution_id = %(execution_id)s
"""

GET_ACTIVE_CLUSTER_RUN_ID_WITH_CLUSTER_ID = """
SELECT active_cluster_runid
FROM cluster_status
WHERE cluster_id = %(cluster_id)s
"""

UPDATE_REMOTE_COMMAND_EXECUTION_STATUS = """
UPDATE remote_command_status
SET status = %(status)s, error_logs_path = %(error_logs_path)s, error_code = %(error_code)s
WHERE execution_id = %(execution_id)s
"""
