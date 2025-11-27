GET_LOGS = """
SELECT logs_url
FROM darwin_image_builder
WHERE task_id = %(task_id)s;
"""

GET_BUILD_STATUS = """
SELECT status
FROM darwin_image_builder
WHERE task_id = %(task_id)s;
"""

GET_IMAGE_TAG = """
SELECT image_tag
FROM darwin_image_builder
WHERE task_id = %(task_id)s;
"""

GET_APP_NAME = """
SELECT app_name
FROM darwin_image_builder
WHERE task_id = %(task_id)s;
"""

GET_BUILD_PARAMS = """
SELECT build_params
FROM darwin_image_builder
WHERE task_id = %(task_id)s;
"""

LIST_ALL_TASKS = """
SELECT *
FROM darwin_image_builder;
"""

CREATE_RUNTIME = """
INSERT INTO darwin_image_builder (task_id, app_name, image_tag, logs_url, build_params, status)
VALUES (%(task_id)s, %(app_name)s, %(image_tag)s, %(logs_url)s, %(build_params)s, %(status)s);
"""

UPDATE_IMAGE_BUILD_STATUS = """
UPDATE darwin_image_builder
SET status = %(status)s
WHERE task_id = %(task_id)s;
"""

GET_WAITING_TASK = """
SELECT * FROM darwin_image_builder WHERE status = 'waiting' ORDER BY task_id LIMIT 1 FOR UPDATE SKIP LOCKED;
"""
