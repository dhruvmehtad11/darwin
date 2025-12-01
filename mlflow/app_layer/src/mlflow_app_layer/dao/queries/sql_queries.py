GET_EXPERIMENT_USER = """
SELECT experiment_permissions.experiment_id, experiment_permissions.user_id, users.username
FROM experiment_permissions INNER JOIN users on experiment_permissions.user_id = users.id
WHERE experiment_permissions.experiment_id = %(experiment_id)s LIMIT 1;
"""
